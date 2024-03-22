FROM ubuntu:20.04
MAINTAINER ayound "48098911@qq.com"
# 内置一些常用的中文字体，避免普遍性乱码
COPY fonts/* /usr/share/fonts/chinese/
RUN sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list &&\
	sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list &&\
	apt-get clean && apt-get update &&\
	apt-get install -y --reinstall ca-certificates &&\
	apt-get clean && apt-get update &&\
	apt-get install -y locales language-pack-zh-hans &&\
	localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8 && locale-gen zh_CN.UTF-8 &&\
    export DEBIAN_FRONTEND=noninteractive &&\
	apt-get install -y tzdata && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime &&\
	apt-get install -y fontconfig ttf-mscorefonts-installer ttf-wqy-microhei ttf-wqy-zenhei xfonts-wqy &&\
	apt-get install -y wget &&\
    cd /tmp &&\
    wget --recursive --no-parent --no-check-certificate https://repo.huaweicloud.com/java/jdk/11.0.2+9/jdk-11.0.2_linux-x64_bin.tar.gz -O /tmp/jdk-11.0.2_linux-x64_bin.tar.gz &&\
	tar -zxf /tmp/jdk-11.0.2_linux-x64_bin.tar.gz && mv /tmp/jdk-11.0.2 /usr/local/ &&\

# 安装 libreoffice
    apt-get install -y libxrender1 libxinerama1 libxt6 libxext-dev libfreetype6-dev libcairo2 libcups2 libx11-xcb1 libnss3 &&\
    wget --recursive --no-parent --no-check-certificate https://mirrors.cloud.tencent.com/libreoffice/libreoffice/stable/7.6.5/deb/x86_64/LibreOffice_7.6.5_Linux_x86-64_deb.tar.gz -O /tmp/LibreOffice_7.6.5_Linux_x86-64_deb.tar.gz &&\
    tar -zxf /tmp/LibreOffice_7.6.5_Linux_x86-64_deb.tar.gz && cd /tmp/LibreOffice_7.6.5.2_Linux_x86-64_deb/DEBS &&\
    dpkg -i *.deb &&\

#  清理临时文件
	rm -rf /tmp/* && rm -rf /var/lib/apt/lists/* &&\
    cd /usr/share/fonts/chinese &&\
    mkfontscale &&\
    mkfontdir &&\
    fc-cache -fv &&\
	
	mkdir /fsearch &&\
	mkdir /fsearch/cache &&\
	mkdir /fsearch/files &&\
	mkdir /fsearch/data 

COPY target/file-search-1.0.6-SNAPSHOT.jar /fsearch/file-search-1.0.6-SNAPSHOT.jar
ENV USER_NAME admin
ENV PASSWORD 123456
ENV KK_BASE_URL default
ENV JAVA_HOME /usr/local/jdk-11.0.2
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:$JAVA_HOME/bin
ENV LANG zh_CN.UTF-8
ENV LC_ALL zh_CN.UTF-8
ENV KK_FILE_DIR /fsearch/cache
ENV SCAN_FILE_PATHS /fsearch/files
ENV DATA_FILE_PATHS /fsearch/data
VOLUME /fsearch/files
VOLUME /fsearch/data
VOLUME /fsearch/cache
EXPOSE 8012
ENTRYPOINT ["java","-jar","/fsearch/file-search-1.0.6-SNAPSHOT.jar"]
CMD ["/bin/bash"]