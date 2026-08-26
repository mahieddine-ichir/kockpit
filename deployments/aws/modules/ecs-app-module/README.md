# ecs-app-module

Terraform module that deploys a single application as an AWS ECS service, fronted by an
existing Application Load Balancer. It is meant to be instantiated once per application/environment
pair (e.g. `checkout-dev`, `checkout-pro`) against shared cluster/VPC/ALB infrastructure created
elsewhere.

It creates / manages:
- ECS task definition (Fargate or EC2) and ECS service
- AWS Cloud Map service discovery entry
- ALB target group(s) and listener rule(s) (plain forward, or Cognito-authenticated)
- The task's security group, IAM roles (task + task execution), and KMS key
- Application auto scaling (CPU/RAM target tracking, optional scheduled up/down scaling)
- Optional blue/green deployments via CodeDeploy
- Optional sidecars: Amazon SSM agent (for ECS Exec-less shell access on Fargate)

## Requirements

| Name | Version |
|------|---------|
| terraform | >= 1.3 |
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

**Rolling vs. blue/green deploys** — by default the module manages a standard ECS rolling-update
service. Set `enable_code_deploy = true` to switch the service's deployment controller to CodeDeploy
and get blue/green deployments instead; this additionally requires `deployment_bucket_name` (where the
generated `appspec.yml` is uploaded) and `aws_lb_test_listener_arn` (the ALB listener used for the
"test" traffic during a deployment — a second target group is created for it). Tune the rollout with
`deployment_config_name`, `wait_time_in_minutes`, and `termination_wait_time_in_minutes`.

**Routing** — by default (`create_listener_rule = true`) the module creates its own unauthenticated,
forward-only ALB listener rule for `path_routing_patterns`. Set `create_cognito_auth_listener_rule =
true` instead to gate the app behind Cognito (requires `cognito_user_pool_arn`,
`cognito_user_pool_client_id`, `cognito_user_pool_domain`). Set `create_listener_rule = false` if the
caller wants to own the listener rule itself and only needs this module's target group
(`aws_lb_target_group` output).

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
| `aws_lb_listener_arn` | ALB listener the path-based routing rule attaches to. |
| `lb_security_group_id` | ALB's security group (the task's security group allows ingress from it). |
| `path_routing_patterns` | ALB listener rule path patterns routed to this app. |
| `task_image_url` | Container image URL for the app container. |
| `kms_secret_arn` | KMS key ARN used to decrypt `secrets` values. |

Commonly overridden (all have defaults):

| Name | Default | Description |
|------|---------|-------------|
| `env` | `""` | Environment name (e.g. `dev`, `pro`); used in most resource names. |
| `region` | `"eu-west-1"` | AWS region. |
| `tags` | `{}` | Tags applied to all taggable resources. |
| `launch_type` | `"FARGATE"` | `"FARGATE"` or `"EC2"`; selects the task definition template. |
| `task_cpu` / `task_memory` | `1024` / `2048` | Task-level CPU units / memory (MiB). |
| `service_port` | `5000` | Container port; also the ALB target group / health check port. |
| `desired_count` | `1` | Initial desired task count. |
| `asg_min_capacity` / `asg_max_capacity` | `1` / `4` | Auto scaling bounds. |
| `environment_variables` | `{}` | Map of plain environment variables for the app container. |
| `secrets` | `{}` | Map of `NAME => secretsmanager/ssm ARN`, injected as ECS `secrets`. |
| `service_healthcheck_path` | `"/actuator/health"` | ALB target group health check path. |
| `enable_code_deploy` | `false` | Use CodeDeploy blue/green instead of rolling deployments. |
| `create_cognito_auth_listener_rule` | `false` | Gate the app behind Cognito auth at the ALB. |
| `enable_periodic_scaling` | `false` | Enable scheduled scaling in addition to target tracking. |
| `sidecar_amazon_ssm_agent_enable` | `false` | Run the SSM agent sidecar. |
| `enable_execute_command` | `false` | Enable ECS Exec on the service. |

See [`variables.tf`](./variables.tf) for the full list, including the CodeDeploy, Cognito, periodic
scaling, and security-group variables not listed above.

## Outputs

| Name | Description |
|------|-------------|
| `aws_lb_target_group` | The primary ALB target group resource. |
| `aws_lb_target_group_2` | The second target group used during CodeDeploy blue/green (`null` unless `enable_code_deploy = true`). |
| `ecs_task_role` | Name of the ECS task IAM role. |
| `aws_iam_role_execution` | The ECS task execution IAM role resource. |
| `aws_iam_role_execution_name` | Deprecated — use `aws_iam_role_execution.name` instead. |
| `ecs_security_group` | ID of the task's security group. |
| `ecs_kms_key_id` | ID of the KMS key created for this application. |
| `ecs_service_name` | Name of the ECS service (rolling or CodeDeploy, whichever is active). |
| `ecs_service_arn` | ARN/ID of the ECS service (rolling or CodeDeploy, whichever is active). |
| `log_group_name` | Name of the app's CloudWatch log group. |
