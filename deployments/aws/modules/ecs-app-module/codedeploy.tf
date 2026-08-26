#Each time we change the value of task definition we will put this file to s3
resource "aws_s3_object" "appspec_object" {
  count  = var.enable_code_deploy ? 1 : 0
  bucket = var.deployment_bucket_name
  key    = "${var.application}/appspec.yml"
  acl    = "private"
  content = templatefile("${path.module}/appspec.yaml.tpl", {
    task_definition_arn = var.launch_type == "FARGATE" ? aws_ecs_task_definition.taskdef_fargate[0].arn : aws_ecs_task_definition.taskdef_ec2[0].arn
    container_name      = "${var.application}-${var.env}"
    container_port      = var.service_port
  })
  tags = var.tags
}

resource "aws_codedeploy_app" "default_code_deploy_app" {
  count            = var.enable_code_deploy ? 1 : 0
  compute_platform = "ECS"
  name             = "${var.application}-codedeploy-application-${var.env}"
}

resource "aws_iam_policy" "code_deploy_policy_kms" {
  count = var.enable_code_deploy ? 1 : 0
  name  = "${var.application}-kms-policy-${var.env}"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "kms:Decrypt",
                "kms:GenerateDataKey",
                "kms:GenerateDataKeyWithoutPlaintext",
                "kms:GenerateDataKeyPairWithoutPlaintext",
                "kms:GenerateDataKeyPair"
            ],
            "Resource": "*"
        }
  ]
}
EOF
}
data "aws_iam_policy_document" "default_code_deploy_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["codedeploy.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "default_code_deploy_role" {
  count              = var.enable_code_deploy ? 1 : 0
  name               = "${var.application}-for-codedeploy-role-${var.env}"
  assume_role_policy = data.aws_iam_policy_document.default_code_deploy_assume_role.json
}

resource "aws_iam_role_policy_attachment" "default_code_deploy_attachement" {
  count      = var.enable_code_deploy ? 1 : 0
  policy_arn = "arn:aws:iam::aws:policy/AWSCodeDeployRoleForECS"
  role       = aws_iam_role.default_code_deploy_role[0].name
}

resource "aws_iam_role_policy_attachment" "code_deploy_attach_kms_policy" {
  count      = var.enable_code_deploy ? 1 : 0
  role       = aws_iam_role.default_code_deploy_role[0].name
  policy_arn = aws_iam_policy.code_deploy_policy_kms[0].arn
}

###############################################################
######################Code deploy group########################
###############################################################
resource "aws_codedeploy_deployment_group" "default_code_deploy_group" {
  count                  = var.enable_code_deploy ? 1 : 0
  app_name               = aws_codedeploy_app.default_code_deploy_app[0].name
  deployment_group_name  = "${var.application}-group-${var.env}"
  service_role_arn       = aws_iam_role.default_code_deploy_role[0].arn
  deployment_config_name = var.deployment_config_name #Shifts % percent of traﬃc

  #configure a deployment group or deployment to automatically roll back when a deployment fails
  auto_rollback_configuration {
    # If you enable automatic rollback, you must specify at least one event type.
    enabled = true
    # The event type or types that trigger a rollback. Supported types are DEPLOYMENT_FAILURE and DEPLOYMENT_STOP_ON_ALARM.
    events = ["DEPLOYMENT_FAILURE"]
  }

  #You can configure how traffic is rerouted to instances in a replacement environment in a blue/green deployment
  blue_green_deployment_config {
    deployment_ready_option {
      action_on_timeout    = "STOP_DEPLOYMENT"        # cela pour dire que je veux redirect le trafic manuellement
      wait_time_in_minutes = var.wait_time_in_minutes #Si tu ne valides pas manuellement la redirection de flux vers le nouveau TG, il va faire rollback pour la dernière version deployé
    }
    # You can configure how instances in the original environment are terminated when a blue/green deployment is successful.
    terminate_blue_instances_on_deployment_success {
      action                           = "TERMINATE"
      termination_wait_time_in_minutes = var.termination_wait_time_in_minutes
    }
  }
  # For ECS deployment, the deployment type must be BLUE_GREEN, and deployment option must be WITH_TRAFFIC_CONTROL.
  deployment_style {
    deployment_option = "WITH_TRAFFIC_CONTROL"
    deployment_type   = "BLUE_GREEN"
  }
  # Configuration block(s) of the ECS services for a deployment group.
  ecs_service {
    cluster_name = var.aws_ecs_cluster_name
    service_name = aws_ecs_service.ecs_service_with_codedeploy[0].name
  }

  #configure the Load Balancer to use in a deployment.
  load_balancer_info {
    target_group_pair_info {
      prod_traffic_route {
        listener_arns = [var.aws_lb_listener_arn]
      }

      test_traffic_route {
        listener_arns = [var.aws_lb_test_listener_arn]
      }

      target_group {
        name = aws_lb_target_group.app.name
      }

      target_group {
        name = aws_lb_target_group.lb_tg_2[0].name
      }
    }
  }
}



resource "null_resource" "trigger_new_deployment" {
  count = var.enable_code_deploy ? 1 : 0
  triggers = {
    task_definition_arn = var.launch_type == "FARGATE" ? aws_ecs_task_definition.taskdef_fargate[0].arn : aws_ecs_task_definition.taskdef_ec2[0].arn
  }

  provisioner "local-exec" {
    command    = <<ENTER_CMD
      aws deploy create-deployment --application-name ${aws_codedeploy_app.default_code_deploy_app[0].name} \
      --deployment-group-name ${aws_codedeploy_deployment_group.default_code_deploy_group[0].deployment_group_name} \
      --revision '{"revisionType":"S3", "s3Location":{"bucket": "${var.deployment_bucket_name}","key": "${var.application}/appspec.yml","bundleType": "YAML"}}'
      ENTER_CMD
    on_failure = fail
  }

}
