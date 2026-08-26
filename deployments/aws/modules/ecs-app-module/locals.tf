locals {
  healthcheck_port_number = var.service_healthcheck_port == "traffic-port" ? var.service_port : tonumber(var.service_healthcheck_port)

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
