variable "environment" { type = string }
variable "username" { type = string }
variable "password" { type = string }
variable "location" { type = string }
variable "rg_name" { type = string }
variable "node" { type = string }
variable "seeds" { type = string }
variable "subnet_id" { type = string }
variable "subnet_cidr" { type = string }
variable "dns_name" { type = string }

resource "azurerm_network_interface" "opensearch-dashboard" {
  name                = "kockpit-opensearch-dashboard-ni-${var.environment}"
  location            = var.location
  resource_group_name = var.rg_name
  ip_configuration {
    name                          = "kockpit-opensearch-dashboard-ip-config-${var.environment}"
    subnet_id                     = var.subnet_id
    private_ip_address_allocation = "Dynamic"
  }
}

resource "azurerm_linux_virtual_machine" "opensearch-dashboard" {
  name     = "opensearch-dashboard-${var.environment}"
  location = var.location
  network_interface_ids = [
    azurerm_network_interface.opensearch-dashboard.id
  ]
  identity {
    type = "SystemAssigned"
  }
  resource_group_name = var.rg_name
  size                = "Standard_F2"
  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }
  source_image_reference {
    publisher = "Canonical"
    offer     = "0001-com-ubuntu-server-jammy"
    sku       = "22_04-lts"
    version   = "latest"
  }
  admin_username                  = var.username
  admin_password                  = var.password
  disable_password_authentication = false
}

resource "azurerm_virtual_machine_extension" "opensearch-dashboard-install" {
  name                 = "opensearch-dashboard-install"
  publisher            = "Microsoft.Azure.Extensions"
  type                 = "CustomScript"
  type_handler_version = "2.0"
  virtual_machine_id   = azurerm_linux_virtual_machine.opensearch-dashboard.id
  protected_settings = <<PROT
  {
    "script": "${base64encode(templatefile("${path.module}/opensearch-dashboard-install.sh", {
  env   = var.environment,
  seeds = var.seeds,
  user  = var.username,
  node  = var.node
}))}"
  }
PROT

tags = {
  env  = var.environment
  node = "opensearch-dashboard-${var.environment}"
}
}

resource "azurerm_network_security_group" "opensearch-dashboard" {
  name                = "kockpit-opensearch-dashboard-sg-${var.environment}"
  location            = var.location
  resource_group_name = var.rg_name
}

resource "azurerm_network_security_rule" "opensearch-5601" {
  name                        = "kockpit-opensearch-dashboard-5601-${var.environment}"
  priority                    = 100
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "5601"
  source_address_prefix       = var.subnet_cidr
  destination_address_prefix  = "*"
  resource_group_name         = var.rg_name
  network_security_group_name = azurerm_network_security_group.opensearch-dashboard.name
}

resource "azurerm_network_interface_security_group_association" "sg-association" {
  network_interface_id      = azurerm_network_interface.opensearch-dashboard.id
  network_security_group_id = azurerm_network_security_group.opensearch-dashboard.id
}

resource "azurerm_private_dns_a_record" "opensearch-node-dns" {
  name                = "opensearch-dashboard"
  resource_group_name = var.rg_name
  ttl                 = 3600
  zone_name           = var.dns_name

  records = [azurerm_linux_virtual_machine.opensearch-dashboard.private_ip_address]
}
