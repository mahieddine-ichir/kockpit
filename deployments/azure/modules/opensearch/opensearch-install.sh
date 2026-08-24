#!/bin/bash

# /dev/sdX naming isn't guaranteed by Azure (it depends on SCSI enumeration
# order). LUN 11 is, so address the data disk via the stable udev symlink.
DATA_DISK=/dev/disk/azure/scsi1/lun11

# The symlink is created asynchronously by udev after attach; wait for it.
for i in $(seq 1 30); do
  [ -e "$DATA_DISK" ] && break
  sleep 1
done

# Only format the data disk the first time it's provisioned; re-running this
# script (e.g. after a topology change that updates the extension) must never
# wipe an already-formatted, already-populated disk.
if ! blkid "$DATA_DISK" > /dev/null 2>&1; then
  mkfs.ext4 "$DATA_DISK"
fi

# Persist the mount by UUID (not by device path, for the same reason as
# above) so the data disk comes back after a reboot instead of leaving
# /mnt/opensearch-data as an empty directory on the OS disk. nofail keeps
# boot from hanging if the disk is ever slow to attach.
DATA_DISK_UUID=$(blkid -s UUID -o value "$DATA_DISK")
FSTAB_ENTRY="UUID=$DATA_DISK_UUID /mnt/opensearch-data ext4 defaults,nofail 0 2"
grep -qF "$FSTAB_ENTRY" /etc/fstab || echo "$FSTAB_ENTRY" >> /etc/fstab

mkdir -p /mnt/opensearch-data
mkdir -p /mnt/opensearch-data/data
mkdir -p /mnt/opensearch-data/logs
chown -R ${user}:${user} /mnt/opensearch-data
chmod 755 /mnt/opensearch-data
if ! mountpoint -q /mnt/opensearch-data; then
  mount "$DATA_DISK" /mnt/opensearch-data
fi

mkdir -p /opensearch/
cd /opensearch/ || exit
wget https://artifacts.opensearch.org/releases/bundle/opensearch/3.2.0/opensearch-3.2.0-linux-x64.tar.gz
tar -xvf opensearch-3.2.0-linux-x64.tar.gz
ln -sf opensearch-3.2.0 opensearch

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

# Opensearch heap
mkdir -p /opensearch/opensearch/config/jvm.options.d
sudo tee /opensearch/opensearch/config/jvm.options.d/heap.options > /dev/null <<EOF
-Xms2g
-Xmx2g
EOF

# Match ownership of the surrounding files
chown -R $(stat -c '%U:%G' /opensearch/opensearch/config/jvm.options) /opensearch/opensearch/config/jvm.options.d
chmod 644 /opensearch/opensearch/config/jvm.options.d/heap.options

systemctl enable opensearch.service
systemctl start opensearch.service
