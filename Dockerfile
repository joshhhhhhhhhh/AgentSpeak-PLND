FROM ubuntu:22.04
LABEL authors="Josh Blondin"

# update the apt package manager
RUN apt-get update
RUN apt-get install -y software-properties-common
RUN apt-get update && apt-get -y install locales

# install common packages
RUN apt-get update && apt-get install -y \
        build-essential \
        cmake \
        make \
        g++ \
        vim \
        git \
        bison \
        flex \
        dos2unix \
        graphviz \
        time \
        bc \
        libboost-all-dev \
        libgmp3-dev \
        gap \
        nauty \
        pkg-config \
        libsystemd-dev \
        swig \
        autoconf \
        libtool \
        antlr3 \
        wget \
        curl \
        file \
        procps \
        perl \
        maven \
        opam \
        zip \
        tar \
        unzip \
        apache2 \
    && apt-get clean 


ENV DEBIAN_FRONTEND=noninteractive

# Adding java
#RUN add-apt-repository -y ppa:linuxuprising/java
#RUN apt-get update
#RUN echo oracle-java15-installer shared/accepted-oracle-license-v1-3 select true | /usr/bin/debconf-set-selections
#RUN apt-get install -y oracle-java15-installer



# Download and install OpenJDK 15
#RUN wget https://github.com/AdoptOpenJDK/openjdk15-binaries/releases/download/jdk-15.0.2%2B7/OpenJDK15U-jdk_x64_linux_hotspot_15.0.2_7.tar.gz \
#    && mkdir -p /opt \
#    && tar -xzf OpenJDK15U-jdk_x64_linux_hotspot_15.0.2_7.tar.gz -C /opt \
#    && mv /opt/jdk-15.0.2+7 /opt/jdk-15 \
#    && rm OpenJDK15U-jdk_x64_linux_hotspot_15.0.2_7.tar.gz
#RUN wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb \
#    && apt install ./jdk-21_linux-x64_bin.deb
#ENV JAVA_HOME="/usr/lib/jvm/jdk-21-oracle-x64"

ENV JAVA_URL=https://download.oracle.com/java/21/latest \
    JAVA_HOME=/usr/java/jdk-21

##
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN set -eux; \
    ARCH="$(uname -m)" && \
    # Java uses just x64 in the name of the tarball
    if [ "$ARCH" = "x86_64" ]; \
        then ARCH="x64"; \
    fi && \
    JAVA_PKG="$JAVA_URL"/jdk-21_linux-"${ARCH}"_bin.tar.gz ; \
    JAVA_SHA256=$(curl "$JAVA_PKG".sha256) ; \
    curl --output /tmp/jdk.tgz "$JAVA_PKG" && \
    echo "$JAVA_SHA256" */tmp/jdk.tgz | sha256sum -c; \
    mkdir -p "$JAVA_HOME"; \
    tar --extract --file /tmp/jdk.tgz --directory "$JAVA_HOME" --strip-components 1

ENV PATH="${JAVA_HOME}/bin:${PATH}"

# install python and related
RUN apt-get install -y python3 python3-dev python3-pip python3-venv python-is-python3 pypy cython3
RUN apt-get install -y python2
RUN pip3 install --upgrade Flask
RUN pip3 install --upgrade flask-cors
RUN pip3 install --upgrade pip
RUN pip3 install --upgrade graphviz
RUN pip3 install --upgrade networkx
RUN pip3 install --upgrade pydot
RUN pip3 install --upgrade typed-argument-parser
RUN pip3 install --upgrade argcomplete
RUN pip3 install --upgrade networkx
RUN pip3 install --upgrade nltk
RUN pip3 install --upgrade automata-lib

#Gradle

ENV GRADLE_VERSION=8.14
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
        && unzip -q gradle-${GRADLE_VERSION}-bin.zip -d /opt \
        && rm gradle-${GRADLE_VERSION}-bin.zip \
        && ln -s /opt/gradle-${GRADLE_VERSION} /opt/gradle

ENV PATH="/opt/gradle/bin:${PATH}"

#Updating Node
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
RUN apt-get update && apt-get install -y nodejs

#Brew
#RUN NONINTERACTIVE=1 /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Grab the other planners (currently just prp)
WORKDIR /PLANNERS



RUN git clone https://github.com/QuMuLab/planner-for-relevant-policies.git prp
RUN cd prp/src && ./build_all -j 8


RUN git clone https://github.com/hstairs/ndcpces.git ndcpces

RUN pip3 install --upgrade pysmt --pre
RUN pysmt-install --confirm-agreement --msat

#Epistemic Jason Reqs
RUN eval $(opam env) && opam init --reinit --disable-sandboxing --yes
RUN eval $(opam env) && opam install touist --yes
RUN git clone https://github.com/Ethavanol/touist-service.git
#RUN cd touist-service && python3 -m server

RUN git clone https://github.com/Ethavanol/epistemic-reasoner.git
RUN cd epistemic-reasoner && npm install

RUN git clone https://github.com/NMAI-lab/epistemic-jason.git
RUN cd epistemic-jason && ./gradlew publishToMavenLocal -x test

#RUN git clone https://github.com/Ethavanol/epistemic-agents.git

#RUN cd epistemic-agents && mv reasoner-config.json.example reasoner-config.json
#RUN cd epistemic-agents && gradle wrapper --gradle-version 8.6
#RUN cd epistemic-agents && ./gradlew publishToMavenLocal -x test

#RUN git clone https://github.com/pysmt/pysmt.git pysmt
#RUN python3 pysmt/install.py --confirm-agreement --msat

#ENV PYSMT_PATH /pysmt
#ENV PYSMT_MSAT_PATH_3 /pysmt/.smt_solvers/mathsat-5.3.8-linux-x86_64/python:/pysmt/.smt_solvers/mathsat-5.3.8-linux-x86_64/python/build/lib.linux-x86_64-3.4
#ENV PYTHONPATH_3 ${PYSMT_PATH}:${PYSMT_MSAT_PATH_3}

#ENV PYTHONPATH ${PYTHONPATH_3}

RUN mkdir /PLANNERS/bin

RUN echo "#!/bin/bash" >> /PLANNERS/bin/prp
RUN echo "/PLANNERS/prp/src/prp \$@" >> /PLANNERS/bin/prp

RUN echo "#!/bin/bash" >> /PLANNERS/bin/ndcpces
RUN echo "python3 /PLANNERS/ndcpces/ndcpces.py -decomposition true -lookahead_mode 0 \$@" >> /PLANNERS/bin/ndcpces

# Update the PATH variable and permissions
ENV PATH="/PLANNERS/bin:${PATH}"
RUN chmod -R 777 /PLANNERS/bin

WORKDIR /AGENTSPEAK-PLND

COPY . .

RUN gradle build
#CMD ["ls"]
ENTRYPOINT ["/bin/bash", "docker_startup.sh"]
#CMD /bin/bash
