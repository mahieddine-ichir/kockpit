output "manifest_json" {
  description = "The generated manifest as a JSON string"
  value       = local.manifest_json
}

output "manifest" {
  description = "The generated manifest as a Terraform object"
  value       = local.manifest
}

output "output_path" {
  description = "Path to the written manifest file"
  value       = local_file.manifest.filename
}
