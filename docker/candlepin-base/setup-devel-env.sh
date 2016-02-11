#!/bin/sh
#
# Sets a system up for a candlepin development environment (minus a db,
# handled separately), and an initial clone of candlepin.

set -e

export JAVA_VERSION=1.8.0
export JAVA_HOME=/usr/lib/jvm/java-$JAVA_VERSION

# Install & configure dev environment
yum install -y epel-release

PACKAGES=(
    hostname
    rsyslog
    wget
    vim-enhanced
    python-pip
    git
    tig
    rubygems
    ruby-devel
    gcc
    tomcat
    java-$JAVA_VERSION-openjdk-devel
    liquibase
    libxml2-python
    openssl
    gettext
    tmux
)

yum install -y ${PACKAGES[@]}

# Setup for autoconf:
mkdir /etc/candlepin
echo "# AUTOGENERATED" > /etc/candlepin/candlepin.conf

cat > /root/.bashrc <<BASHRC
if [ -f /etc/bashrc ]; then
	. /etc/bashrc
fi

export HOME=/root
export JAVA_HOME=/usr/lib/jvm/java-$JAVA_VERSION
BASHRC

# Create an initial candlepin checkout at /candlepin in image to help decrease
# the amount of time to run tests later on.
git clone https://github.com/candlepin/candlepin.git /candlepin
cd /candlepin

# Allow for grabbing specific pull requests
git config --add remote.origin.fetch "+refs/pull/*:refs/remotes/origin/pr/*"
git pull

# Install all ruby deps:
gem install bundler
bundle install

# Installs all Java deps into the image, big time saver
# We run checkstyle explicitly here so it'll pull down its deps as well
buildr artifacts
buildr checkstyle || true
