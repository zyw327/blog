FROM centos:7
WORKDIR /root
RUN yum install -y git

# Make ssh dir
RUN mkdir /root/.ssh/
COPY id_rsa.pub /root/.ssh/id_rsa.pub
COPY id_rsa /root/.ssh/id_rsa

# Copy over private key, and set permissions
# ADD id_rsa /root/.ssh/id_rsa
RUN chmod 700 /root/.ssh/id_rsa
RUN chown -R root:root /root/.ssh

# Create known_hosts
RUN touch /root/.ssh/known_hosts

# Remove host checking
RUN echo "Host gitee.com\n\tStrictHostKeyChecking no\n" >> /root/.ssh/config


COPY apache-maven-3.6.3-bin.tar.gz /root
COPY jdk-8u171-linux-x64.tar.gz /root
RUN cd /root
RUN tar -zxvf apache-maven-3.6.3-bin.tar.gz
RUN tar -zxvf jdk-8u171-linux-x64.tar.gz
RUN ssh-keyscan gitee.com >> /root/.ssh/known_hosts
RUN git clone https://gitee.com/okgoes/lawyer-service

RUN cd lawyer-service

RUN mvn clean compile package -Dmaven.test.skip=true -Ptest

RUN echo "export JAVA_HOME=/root/jdk1.8.0_171" >> /etc/profile
RUN echo "export CLASSPATH=.:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar" >> /etc/profile
RUN echo "export MAVEN_HOME=/usr/software/apache-maven-3.6.1" >> /etc/profile
RUN echo "export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH" >> /etc/profile

RUN source /etc/profile
