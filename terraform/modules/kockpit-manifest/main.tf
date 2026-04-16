locals {
  feature_flipping_entries = [
    for svc in var.feature_flipping_services : {
      type = "feature-flipping"
      name = svc.name
      id   = svc.id
      config = {
        keys = [
          for k in svc.keys : merge(
            { key = k.key, enabled = k.enabled },
            k.expirationDate != null ? { expirationDate = k.expirationDate } : {},
            k.comment != null ? { comment = k.comment } : {}
          )
        ]
      }
    }
  ]

  dyna_config_entries = [
    for svc in var.dyna_config_services : {
      type = "dyna-config"
      name = svc.name
      id   = svc.id
      config = {
        keys = [
          for k in svc.keys : merge(
            { key = k.key, value = k.value },
            k.description != null ? { description = k.description } : {}
          )
        ]
      }
    }
  ]

  audit_entries = [
    for svc in var.audit_services : {
      type = "audit"
      name = svc.name
      id   = svc.id
      config = merge(
        {
          columns = svc.columns
          search_columns = [
            for sc in svc.search_columns : merge(
              { name = sc.name, type = sc.type },
              sc.path != null ? { path = sc.path } : {},
              sc.options != null ? { options = sc.options } : {}
            )
          ]
        },
        length(svc.saved_filters) > 0 ? { saved_filters = svc.saved_filters } : {}
      )
    }
  ]

  manifest = {
    domain   = var.domain
    env      = var.env
    name     = var.name
    services = concat(local.feature_flipping_entries, local.dyna_config_entries, local.audit_entries)
  }

  manifest_json         = jsonencode(local.manifest)
  resolved_out_path     = coalesce(var.output_path, "${path.root}/${var.domain}-${var.env}-manifest.json")
  allowed_service_types = toset(["audit", "dyna-config", "feature-flipping"])
}

check "service_types" {
  assert {
    condition = alltrue([
      for s in local.manifest.services : contains(local.allowed_service_types, s.type)
    ])
    error_message = "All service types must be one of: audit, dyna-config, feature-flipping."
  }
}

resource "local_file" "manifest" {
  content  = local.manifest_json
  filename = local.resolved_out_path
}
