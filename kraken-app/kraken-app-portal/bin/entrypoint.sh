#!/bin/sh

for var in $(env | grep RUNTIME_); do
    IFS='=' read -r key value << EOF
$var
EOF

    echo "Work on replacing env var $key"
    find . -type f -exec sed -i "s|$key|$value|g" {} \;
done
