#!/bin/bash

mkfs.ext4 /dev/sdc
mkdir -p /mnt/opensearch-data
mkdir -p /mnt/opensearch-data/data
mkdir -p /mnt/opensearch-data/logs
chown -R ${user}:${user} /mnt/opensearch-data
chmod 755 /mnt/opensearch-data
mount /dev/sdc /mnt/opensearch-data

mkdir /opensearch/
cd /opensearch/ || exit
wget https://artifacts.opensearch.org/releases/bundle/opensearch/3.2.0/opensearch-3.2.0-linux-x64.tar.gz
tar -xvf opensearch-3.2.0-linux-x64.tar.gz
ln -s opensearch-3.2.0 opensearch

swapoff -a
echo "vm.max_map_count=262144" >> /etc/sysctl.conf
sudo sysctl -p

cat <<EOF > /opensearch/opensearch/config/opensearch.yml
plugins.security.disabled: true
cluster.name: kockpit
node.name: opensearch-${index}-${env}
path.data: /mnt/opensearch-data/data
path.logs: /mnt/opensearch-data/logs
discovery.seed_hosts: [${seeds}]
network.host: opensearch-${index}-${env}
cluster.initial_cluster_manager_nodes: [${seeds}]
EOF

touch /opensearch/start-opensearch.sh
chmod +x /opensearch/start-opensearch.sh

cat <<FOO > /opensearch/start-opensearch.sh
#!/bin/bash
/opensearch/opensearch/bin/opensearch
FOO

cat <<FOO > /etc/systemd/system/opensearch.service
[Unit]
Description=Opensearch Application Server
After=syslog.target network.target

[Service]
Type=idle
User=${user}
Group=${user}
RemainAfterExit=yes
LimitNOFILE=102642
ExecStart=/opensearch/start-opensearch.sh
StandardOutput=null

[Install]
WantedBy=multi-user.target
FOO

systemctl enable opensearch.service
systemctl start opensearch.service
