#!/bin/bash

mkdir -p /opensearch-dashboards/
cd /opensearch-dashboards/ || exit
wget https://artifacts.opensearch.org/releases/bundle/opensearch-dashboards/3.2.0/opensearch-dashboards-3.2.0-linux-x64.tar.gz
tar -xf opensearch-dashboards-3.2.0-linux-x64.tar.gz
ln -sf opensearch-dashboards-3.2.0 opensearch-dashboards

swapoff -a
echo "vm.max_map_count=262144" >> /etc/sysctl.conf
sudo sysctl -p

cat <<EOF > /opensearch-dashboards/opensearch-dashboards/config/opensearch_dashboards.yml
opensearch.hosts: [${seeds}]
server.host: ${node}
EOF

/opensearch-dashboards/opensearch-dashboards/bin/opensearch-dashboards-plugin remove securityDashboards

cat <<FOO > /etc/systemd/system/opensearch-dashboards.service
[Unit]
Description=Opensearch Dashboards Application Server
After=syslog.target network.target

[Service]
Type=idle
User=${user}
Group=${user}
RemainAfterExit=yes
LimitNOFILE=102642
ExecStart=/opensearch-dashboards/opensearch-dashboards/bin/opensearch-dashboards
StandardOutput=null

[Install]
WantedBy=multi-user.target
FOO

systemctl enable opensearch-dashboards.service
systemctl start opensearch-dashboards.service
