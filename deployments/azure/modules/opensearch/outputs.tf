# VM Outputs
output "vm_id" {
  description = "ID of the OpenSearch node VM"
  value       = azurerm_linux_virtual_machine.opensearch-node.id
}

output "private_ip_address" {
  description = "Private IP address of the OpenSearch node"
  value       = azurerm_linux_virtual_machine.opensearch-node.private_ip_address
}

output "identity_principal_id" {
  description = "Principal ID of the OpenSearch node's system-assigned managed identity"
  value       = azurerm_linux_virtual_machine.opensearch-node.identity[0].principal_id
}

# Network Outputs
output "network_interface_id" {
  description = "ID of the OpenSearch node's network interface"
  value       = azurerm_network_interface.ni.id
}

# DNS Outputs
output "dns_record_name" {
  description = "Name of the private DNS A record for the OpenSearch node"
  value       = azurerm_private_dns_a_record.opensearch-node-dns.name
}

output "dns_fqdn" {
  description = "Fully qualified domain name of the OpenSearch node's private DNS record"
  value       = azurerm_private_dns_a_record.opensearch-node-dns.fqdn
}

# Storage Outputs
output "data_disk_id" {
  description = "ID of the OpenSearch node's data disk"
  value       = azurerm_managed_disk.data_disk_256.id
}
