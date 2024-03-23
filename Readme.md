# 说明

我有大量的文档需要全文检索，放在nas上，找遍了软件没有找到合适的，譬如某些软件用pg或Mysql存储文档内容，文档数量上几万的时候，在nas的机器上全文检索就没法打开。

我又需要文件预览功能，不想把文件发到公网上，用WPS或微软的公有云服务预览，我只要自己预览就可以了。

所以编写了这个软件，功能非常简单，就是建立全文索引，搜索文件和预览文件。

在NAS上基本上可以做到全文检索秒搜文件。

索引是采用Lucene，预览采用kkfilepreview。

可以使用maven编译，基于springboot，单jar运行。

完全开源，基于apache协议，可自行修改，开源地址：[GitHub - ayoundzw/file-search: 文件全文检索工具](https://github.com/ayoundzw/file-search)

因为是只花了几天时间手搓了一个程序，请不要吐槽代码质量问题，如有问题，请自行fork修改

在nas中只能使用docker。

# 编译打包

## 第一步

    安装LibreOffice，这个软件使用了kkfilepreview，它依赖LibreOffice，请安装7.6.5版本。

## 第二步

    maven打包，请跳过单元测试

## 第三步(可选)

    docker打包

    docker build --tag ayound/file-search:1.0.6 .

    我已经打好docker镜像了，可以使用ayound/file-search:1.0.6直接拉取



# Docker安装和配置

## 第一步

    拉取镜像 ayound/file-search:1.0.6

## 第二步

需要配置的参数映射如下：

1.端口映射

docker使用的端口是8012，请映射到主机端口

2.磁盘映射

/fsearch/files 映射需要扫描的文件夹

/fsearch/data 映射保存的数据路径

/fsearch/cache 映射预览文件时转换的PDF和图片

3.环境变量

USER_NAME 默认为admin，登录的用户名，这个系统非常简单，支持单用户登录，是基于spring security做的，如有需要，请自行下载源代码修改

PASSWORD 默认为123456，登录的密码，必须修改

## 第三步

启动docker



## 访问方法



http://[ip]:[port]/fsearch，如[http://localhost:8012/fsearch]注意：必须加/fsearch，我这里为了方便nginx的二级目录代理，加了个目录/fsearch，如果想修改，请自行下载源代码修改



# 使用方法

## 开始扫描文档

在界面左下角有一个小按钮，点击就可以开始扫描文档，扫描过程中即可以检索文档

## 查询文档

目前做了4种查询方式：

1.根据文件名（含路径）精确匹配，支持空格，如 输入【数据 治理 上海】会把文件名中包含这三个词的文件查出来。也支持减号，如输入【数据 治理 -上海】，会查询数据和治理，去掉匹配上海的路径。

2.根据文件名（含路径）智能匹配，采用分词方式，可以输入一句话，软件自动中文分词，根据分词查询文件名。

3.根据文件内容精确匹配，支持空格，如输入【数据 治理 上海】会把文件内容中包含这三个词的文件查出来，注意：这里的分词是常用的分词，如果要用词曲，请自己开发

4.根据文件内容智能匹配，采用分词方式，可以输入一句话，软件自动中文分词，根据分词查询文件内容。



# 其他

1.超过200M的文档不支持预览，文档太大，预览太慢，不如下载下来本地打开

2.预览时只转换了前5页，这个也可以配置，可以配置一个ENV变量：KK_OFFICE_PAGERANGE来控制，默认值是1-5，改为false则全转换，不过不建议改，性能太差。



## Nginx代理

如果通过代理访问时，kkfilepreview获取文件地址会出错，需要加一个头X-Forwarded-For，示例如下：



```nginx
location /fsearch/ {
      proxy_pass http://[ip]:8012/fsearch/;#替换为自己的IP
      proxy_redirect off;
      proxy_set_header Host [host];#替换为自己的Host，如域名 
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Base-Url $scheme://$host/fsearch;#这句是关键，kkfilepreview根据这个找路径，配置不对的话无法预览
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; 
}

```


