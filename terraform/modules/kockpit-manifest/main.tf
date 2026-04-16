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

  manifest_json     = jsonencode(local.manifest)
  resolved_s3_key    = coalesce(var.s3_key, "${var.domain}-${var.env}-manifest.json")
  resolved_blob_name = coalesce(var.azure_blob_name, "${var.domain}-${var.env}-manifest.json")
  resolved_out_path  = coalesce(var.output_path, "${path.root}/${var.domain}-${var.env}-manifest.json")
}

locals {
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

resource "aws_s3_object" "manifest" {
  count        = var.s3_bucket != null ? 1 : 0
  bucket       = var.s3_bucket
  key          = local.resolved_s3_key
  content      = local.manifest_json
  content_type = "application/json"

  tags = {
    Domain      = var.domain
    Environment = var.env
    ManagedBy   = "Terraform"
  }
}

resource "azurerm_storage_blob" "manifest" {
  count                  = var.azure_storage_container != null ? 1 : 0
  name                   = local.resolved_blob_name
  storage_account_name   = var.azure_storage_account
  storage_container_name = var.azure_storage_container
  type                   = "Block"
  source_content         = local.manifest_json
  content_type           = "application/json"
}