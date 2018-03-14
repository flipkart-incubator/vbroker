#!/bin/bash

set -e

pkg_name=vbroker-broker
pkg_version=0.1
pkg_release=1
pkg_arch=amd64
git_commit=todo

if [ -d "deb/" ]; then
    rm -rf deb/*
fi

svc_name=${pkg_name}
file_dir=/opt/${pkg_name}
local_file_dir=deb$file_dir

default_config_dir=/etc/default
mkdir -p deb$default_config_dir
svc_defaults_file=$default_config_dir/$svc_name
local_svc_defaults_file=deb$svc_defaults_file

mkdir -p ${local_file_dir}/libs/dependencies
mkdir -p ${local_file_dir}/config

cp -r build/dependencies/* ${local_file_dir}/libs/dependencies/
cp -r build/libs/* ${local_file_dir}/libs/
cp -r src/main/resources/* ${local_file_dir}/config/

meta_dir=deb/DEBIAN
mkdir -p $meta_dir

svc_impl_dir=/etc/sv/$svc_name
local_svc_impl_dir=deb$svc_impl_dir
mkdir -p $local_svc_impl_dir/log
svc_dir=/etc/service/$svc_name
log_dir=/var/log/$svc_name

#local_libs_dir=

vbroker_workdir=/var/tmp/$svc_name
vbroker_user=$svc_name
vbroker_group=$svc_name
vbroker_gid=10129
vbroker_uid=10129

cat > $meta_dir/postinst <<EOF
#!/bin/bash

if ! getent group $vbroker_group > /dev/null; then
    groupadd -g $vbroker_gid $vbroker_group
fi

if ! getent passwd $vbroker_uid > /dev/null; then
    adduser --system --uid $vbroker_uid --home $vbroker_workdir --ingroup $vbroker_group --disabled-password --shell /bin/false $vbroker_user
fi

mkdir -p $log_dir
chown $vbroker_user:$vbroker_group $log_dir

ln -s $svc_impl_dir $svc_dir

EOF

cat > $meta_dir/prerm <<EOF
#!/bin/bash

rm -f $svc_dir
EOF

cat > $meta_dir/control <<EOF
Package: $pkg_name
Version: ${pkg_version}-${pkg_release}
Section: logsvc
Priority: standard
Depends: daemontools, daemontools-run, oracle-java8-jdk
Architecture: $pkg_arch
Provides: $pkg_name
Maintainer: varadhi-dev@flipkart.com
Description: VBroker server package
Commit: ${git_commit}
EOF

chmod +x $meta_dir/postinst $meta_dir/prerm

cat > $local_svc_defaults_file <<EOF
FD_LIMIT=500000
EOF

cat > $local_svc_impl_dir/run <<EOF
#!/bin/bash

cd $vbroker_workdir

source $svc_defaults_file
ulimit -n \$FD_LIMIT

exec java -cp ${file_dir}/libs/dependencies/*:${file_dir}/libs/*: -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=24551 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false $HEAP_CONFIG -server -XX:+UnlockCommercialFeatures -Djava.net.preferIPv4Stack=true -XX:+FlightRecorder -Djava.awt.headless=true -Xloggc:${GC_LOG_FILE} -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M -verbose:gc -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=24505 com.flipkart.vbroker.VBrokerApp ${file_dir}/config/broker.properties 2>&1

EOF

cat > $local_svc_impl_dir/log/run <<EOF
#!/bin/bash

exec setuidgid $vbroker_user multilog t $log_dir
EOF

chmod +x $local_svc_impl_dir/run $local_svc_impl_dir/log/run

dpkg-deb -b deb

mv deb.deb ${pkg_name}_${pkg_version}-${pkg_release}_${pkg_arch}.deb
