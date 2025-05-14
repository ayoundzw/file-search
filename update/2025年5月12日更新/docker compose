绿联云/NAS增强搜索镜像，使用docker compose部署：
services:
  walkerman:
    image: registry.cn-beijing.aliyuncs.com/zdbit/wk_fsearch:20250512
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
