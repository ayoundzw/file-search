# 说明

基于原项目ayoundzw/file-search进行二开，开源地址：[GitHub - ayoundzw/file-search: 文件全文检索工具](https://github.com/ayoundzw/file-search)

使程序更符合NAS增强搜索的使用场景，

尤其是对绿联云效果非常明显，绿联的搜索真的是又慢又卡又不全，一言难尽！

请替换release使用，代码部分稍候更新，主要的更新都在update目录。

目前无法解决搜索结果中包含路径但是路径中不包含文件名的问题，如果有人可以解决，最好不过。

# 调整部分

```change
1.取消了全文检索，时间太久了，全盘文件过多，全文检索硬盘扛不住造。
2.因原来的业务逻辑中有文件内容识别，所以增加了自动创建一个默认内容并加载的方法。
3.界面显示由默认10条改为30条，提供检索效率。
4.检索结果直接提供NAS路径，方便双击用资源管理器或浏览器打开，因为原始的/fsearch路径对于我们使用者没什么用。
5.文字配色微调。
```

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

## docker compose

```docker
services:
  walkerman:
    image: registry.cn-beijing.aliyuncs.com/zdbit/wk_fsearch:latest
    container_name: wk_fsearch
    environment:
      - USER_NAME=admin
      - PASSWORD=123456
    volumes:
      - /volume1:/fsearch/files/volume1
      - /volume2:/fsearch/files/volume2
      - /volume2/docker/wk_fsearch/cache:/fsearch/cache
      - /volume2/docker/wk_fsearch/data:/fsearch/data
    
    ports:
      - "8012:8012"
    restart: unless-stopped
```

## 访问方法



http://[ip]:[port]/fsearch，如[http://localhost:8012/fsearch]注意：必须加/fsearch，我这里为了方便nginx的二级目录代理，加了个目录/fsearch，如果想修改，请自行下载源代码修改



# 使用方法

## 开始扫描文档

在界面左下角有一个小按钮，点击就可以开始扫描文档，扫描过程中即可以检索文档

## 查询文档

目前修改为2种查询方式：

1.根据文件名（含路径）精确匹配，支持空格，如 输入【数据 治理 上海】会把文件名中包含这三个词的文件查出来。也支持减号，如输入【数据 治理 -上海】，会查询数据和治理，去掉匹配上海的路径。

2.根据文件名（含路径）智能匹配，采用分词方式，可以输入一句话，软件自动中文分词，根据分词查询文件名。



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


