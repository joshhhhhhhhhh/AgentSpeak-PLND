FROM ubuntu:22.04
LABEL authors="Josh Blondin"

# update the apt package manager
RUN apt-get update
RUN apt-get install -y software-properties-common
RUN apt-get update && apt-get -y install locales

# install common packages
RUN apt-get install -y \
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
        curl


RUN DEBIAN_FRONTEND=noninteractive apt-get install -y expect

# Adding java
RUN add-apt-repository -y ppa:linuxuprising/java
RUN apt-get update
RUN echo oracle-java17-installer shared/accepted-oracle-license-v1-3 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java17-installer

# install python and related
RUN apt-get install -y python3 python3-dev python3-pip python3-venv python-is-python3 pypy cython3
RUN apt-get install -y python2
RUN pip3 install --upgrade pip
RUN pip3 install --upgrade graphviz
RUN pip3 install --upgrade networkx
RUN pip3 install --upgrade pydot
RUN pip3 install --upgrade typed-argument-parser
RUN pip3 install --upgrade argcomplete
RUN pip3 install --upgrade networkx
RUN pip3 install --upgrade nltk
RUN pip3 install --upgrade automata-lib



# Grab the other planners (currently just prp)
WORKDIR /PLANNERS

RUN git clone https://github.com/QuMuLab/planner-for-relevant-policies.git prp
RUN cd prp/src && ./build_all -j 8


RUN git clone https://github.com/hstairs/ndcpces.git ndcpces

RUN pip3 install --upgrade pysmt --pre
RUN pysmt-install --confirm-agreement --msat

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

WORKDIR /PELEUS

COPY . .
CMD ["./gradlew"]
#CMD /bin/bash