# ecs-app-module

Terraform module that deploys a single application as an AWS ECS service, optionally fronted by an
existing Application Load Balancer. It is meant to be instantiated once per application/environment
pair (e.g. `checkout-dev`, `checkout-pro`) against shared cluster/VPC/ALB infrastructure created
elsewhere.

It creates / manages:
- ECS task definition (Fargate or EC2) and ECS service
- AWS Cloud Map service discovery entry
- Optionally, an ALB target group and listener rule(s) (plain forward, or Cognito-authenticated) —
  disable for internal-only services with no ALB exposure
- The task's security group, IAM roles (task + task execution), and KMS key
- Application auto scaling (CPU/RAM target tracking, optional scheduled up/down scaling)
- Optional sidecars: Amazon SSM agent (for ECS Exec-less shell access on Fargate)

## Requirements

| Name | Version |
|------|---------|
| terraform | >= 1.4 |
| aws | (see root module's provider configuration) |

## Quick start

Minimal Fargate service, rolling deployments, plain (unauthenticated) path-based routing:

```hcl
module "checkout_api" {
  source = "../../modules/ecs-app-module"

  application = "checkout-api"
  env         = "dev"
  stack       = "checkout"
  region      = "eu-west-1"
  account_id  = "123456789012"
  tags        = { team = "checkout", env = "dev" }

  # Cluster / networking supplied by shared infra modules
  aws_ecs_cluster_id                          = module.ecs_cluster.id
  aws_ecs_cluster_name                        = module.ecs_cluster.name
  vpc                                         = module.vpc.vpc
  subnets                                     = module.vpc.private_subnets
  aws_service_discovery_private_dns_namespace = module.service_discovery.namespace

  # Load balancer
  aws_lb_listener_arn  = module.alb.https_listener_arn
  lb_security_group_id = module.alb.security_group_id
  path_routing_patterns = ["/checkout/*"]

  # Task
  task_image_url = "123456789012.dkr.ecr.eu-west-1.amazonaws.com/checkout-api:latest"
  task_cpu       = 512
  task_memory    = 1024
  service_port   = 8080

  environment_variables = {
    SPRING_PROFILES_ACTIVE = "dev"
  }
  secrets = {
    DB_PASSWORD = "arn:aws:secretsmanager:eu-west-1:123456789012:secret:checkout/db-password"
  }
  kms_secret_arn = "arn:aws:kms:eu-west-1:123456789012:key/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

`secrets` values must be Secrets Manager or SSM Parameter Store ARNs; `kms_secret_arn` must be the
KMS key protecting them (the task execution role is granted `kms:Decrypt` on it).

## Feature toggles

**Fargate vs. EC2** — `launch_type` (default `"FARGATE"`) selects which task definition template is
rendered and which task-role IAM policy is attached. When switching to `"EC2"`, also set
`task_requires_compatibilities = ["EC2"]` and `service_launch_type = "EC2"` (the module does not flip
these automatically).

**ALB exposure** — by default (`enable_load_balancer = true`) the module creates a target group, a
health-check security group rule, and registers the service with the target group; the service also
requires `aws_lb_listener_arn`, `lb_security_group_id`, and `path_routing_patterns` in this mode. Set
`enable_load_balancer = false` for internal-only services with no ALB involvement at all (e.g. reachable
only via service discovery) — the target group, listener rule(s), and health-check security group rule
are all skipped, and the task's security group no longer opens ingress from `lb_security_group_id`. In
that case, set `in_security_groups` to whichever security groups need to reach the service directly,
otherwise the task's security group ends up with no ingress source.

**Routing** — when `enable_load_balancer = true` and (by default, `create_listener_rule = true`), the
module creates its own unauthenticated, forward-only ALB listener rule for `path_routing_patterns`. Set
`create_cognito_auth_listener_rule = true` instead to gate the app behind Cognito (requires
`cognito_user_pool_arn`, `cognito_user_pool_client_id`, `cognito_user_pool_domain`). Set
`create_listener_rule = false` if the caller wants to own the listener rule itself and only needs this
module's target group (`aws_lb_target_group` output).

**Auto scaling** — CPU/RAM target-tracking scaling is always active between `asg_min_capacity` and
`asg_max_capacity`, targeting `threshold_high_cpu` / `threshold_high_ram`. Set
`enable_periodic_scaling = true` to additionally scale on a schedule (e.g. business hours), configured
via the `periodic_upscale_*` / `periodic_downscale_*` variables.

**SSM agent sidecar** — set `sidecar_amazon_ssm_agent_enable = true` to run an `amazon-ssm-agent`
sidecar container (self-registers as a managed instance on task start, deregisters on stop). Separate
from `enable_execute_command`, which turns on plain ECS Exec.

**Logging** — the app container always logs to CloudWatch (`awslogs` driver, one log group per
service). `log_mode` / `log_buffer_size` tune the awslogs driver's buffering behavior.

## Inputs

Required (no default — must be supplied by the caller):

| Name | Description |
|------|-------------|
| `application` | Application name; used to derive most resource names. |
| `stack` | Stack name; used in the KMS key description. |
| `account_id` | AWS account ID the module deploys into. |
| `aws_ecs_cluster_id` | Target ECS cluster ID. |
| `aws_ecs_cluster_name` | Target ECS cluster name. |
| `vpc` | VPC object (as returned by the VPC module) the task and target group are created in. |
| `subnets` | Subnets the task's ENI is placed in. |
| `aws_service_discovery_private_dns_namespace` | Cloud Map private DNS namespace for service discovery. |
| `task_image_url` | Container image URL for the app container. |
| `kms_secret_arn` | KMS key ARN used to decrypt `secrets` values. |

`aws_lb_listener_arn`, `lb_security_group_id`, and `path_routing_patterns` default to empty but are
effectively required too whenever `enable_load_balancer = true` (the default); the module doesn't
validate this, so omitting them just produces an ALB target group/listener rule with no valid
attachment. Leave them unset only when `enable_load_balancer = false`.

Commonly overridden (all have defaults):

| Name | Default | Description |
|------|---------|-------------|
| `enable_load_balancer` | `true` | Attach the service to an ALB (target group, listener rule(s), health-check SG rule). Set `false` for internal-only services. |
| `aws_lb_listener_arn` | `""` | ALB listener the path-based routing rule attaches to. |
| `lb_security_group_id` | `""` | ALB's security group (the task's security group allows ingress from it). |
| `path_routing_patterns` | `[]` | ALB listener rule path patterns routed to this app. |
| `env` | `""` | Environment name (e.g. `dev`, `pro`); used in most resource names. |
| `region` | `"eu-west-1"` | AWS region. |
| `tags` | `{}` | Tags applied to all taggable resources. |
| `launch_type` | `"FARGATE"` | `"FARGATE"` or `"EC2"`; selects the task definition template. |
| `task_cpu` / `task_memory` | `1024` / `2048` | Task-level CPU units / memory (MiB). |
| `service_port` | `5000` | Container port; also the ALB target group / default health check port. |
| `service_healthcheck_port` | `"traffic-port"` | `"traffic-port"` (same as `service_port`) or a separate numeric port (e.g. an Actuator management port); opened on the security group and container port mappings automatically when different from `service_port`. |
| `desired_count` | `1` | Initial desired task count. |
| `asg_min_capacity` / `asg_max_capacity` | `1` / `4` | Auto scaling bounds. |
| `environment_variables` | `{}` | Map of plain environment variables for the app container. |
| `secrets` | `{}` | Map of `NAME => secretsmanager/ssm ARN`, injected as ECS `secrets`. |
| `ecr_repository_arns` | `["*"]` | Repository ARN(s) the execution role may pull `task_image_url` from; narrow for least privilege once known. |
| `service_healthcheck_path` | `"/actuator/health"` | ALB target group health check path. |
| `create_cognito_auth_listener_rule` | `false` | Gate the app behind Cognito auth at the ALB. |
| `enable_periodic_scaling` | `false` | Enable scheduled scaling in addition to target tracking. |
| `sidecar_amazon_ssm_agent_enable` | `false` | Run the SSM agent sidecar. |
| `enable_execute_command` | `false` | Enable ECS Exec on the service. |

See [`variables.tf`](./variables.tf) for the full list, including the Cognito, periodic scaling, and
security-group variables not listed above.

## Outputs

| Name | Description |
|------|-------------|
| `aws_lb_target_group` | The ALB target group resource (`null` when `enable_load_balancer = false`). |
| `ecs_task_role` | Name of the ECS task IAM role. |
| `aws_iam_role_execution_name` | Name of the ECS task execution IAM role. |
| `aws_iam_role_execution_arn` | ARN of the ECS task execution IAM role. |
| `ecs_security_group` | ID of the task's security group. |
| `ecs_kms_key_id` | ID of the KMS key created for this application. |
| `ecs_service_name` | Name of the ECS service. |
| `ecs_service_arn` | ARN/ID of the ECS service. |
| `log_group_name` | Name of the app's CloudWatch log group. |
