locals {
  environment_variables = [
    for k, v in var.environment_variables : {
      name  = k
      value = v
    }
  ]

  secrets = [
    for k, v in var.secrets : {
      name      = k
      valueFrom = v
    }
  ]
}
