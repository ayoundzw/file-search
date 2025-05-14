//search.js 我提供了完整代码，替换即可：https://github.com/zdbit/file-search/blob/main/src/main/resources/static/file-search/js/search.js
function formatDate(d) {
    let date = new Date(d);
    let year = date.getFullYear();
    let month = date.getMonth() + 1;
    let day = date.getDate();
    let hour = date.getHours();
    let minute = date.getMinutes();
    let second = date.getSeconds();

    return `${year}-${month}-${day}- ${hour}:${minute}:${second}`;
}

function updatePagination(page, count) {
    const pagination = $('#pagination');
    let pageSize = $("#pageSize").val() * 1;
    if (!pagination) return;

    const totalPages = Math.ceil(count / pageSize); // 假设每页显示10条数据
    let html = '';

    // 添加第一页和前一页
    if (page > 1) {
        html += `<li class="page-item"><a class="page-link" href="#" data-page="1"><<</a></li>`;
        html += `<li class="page-item"><a class="page-link" href="#" data-page="${page - 1}"><</a></li>`;
    } else {
        html += `<li class="page-item disabled"><a class="page-link" href="#"><<</a></li>`;
        html += `<li class="page-item disabled"><a class="page-link" href="#"><</a></li>`;
    }

    // 添加当前页前后5页
    for (let i = Math.max(1, page - 2); i <= Math.min(totalPages, page + 2); i++) {
        if (i === page) {
            html += `<li class="page-item active"><a class="page-link" href="#">${i}</a></li>`;
        } else {
            html += `<li class="page-item"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
        }
    }

    // 添加下一页和最后一页
    if (page < totalPages) {
        html += `<li class="page-item"><a class="page-link" href="#" data-page="${page + 1}">></a></li>`;
        html += `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">>></a></li>`;
    } else {
        html += `<li class="page-item disabled"><a class="page-link" href="#">></a></li>`;
        html += `<li class="page-item disabled"><a class="page-link" href="#">>></a></li>`;
    }

    pagination.html(html);
    $("#totalCount").text(`共${count}条`);
    $(".page-link").off("click");
    $(".page-link").on("click", function(event) {
        var page = $(event.target).attr("data-page")
        if (page) {
            searchFile(page);
        }
    });

}

function searchFile(gotoPage) {
    let page = gotoPage || 1;
    let query = $("#query").val();
    let queryType = $("#queryType").val();
    let orderType = $("#orderType").val();
    let sort = $("#queryOrder").val();
    let sortField = "s_time";
    let direct = "false";
    if (sort == "1") {
        sortField = "s_size";
        direct = "true";
    } else if (sort == "2") {
        sortField = "s_size";
        direct = "false";
    } else if (sort == "3") {
        sortField = "s_time";
        direct = "true";
    } else if (sort == "4") {
        sortField = "s_time";
        direct = "false";
    }
    let pageSize = $("#pageSize").val() * 1;
    $.ajax({
        url: "/fsearch/api/search",
        type: "GET",
        data: {
            text: query,
            type: queryType,
            sortField: sortField,
            direct: direct,
            page: page,
            pageSize: pageSize
        },
        success: function(response) {
            updateResultList(response);
        },
        error: function(error) {
            console.log(error);
        }
    });
}


function startScan() {
    $.ajax({
        url: "/fsearch/api/start",
        type: "GET",
        success: function(response) {
            const toast = new bootstrap.Toast($("#startTaskToast"))
            toast.show()
        },
        error: function(error) {
            console.log(error);
        }
    });
}


function stopScan() {
    $.ajax({
        url: "/fsearch/api/end",
        type: "GET",
        success: function(response) {
            const toast = new bootstrap.Toast($("#endTaskToast"))
            toast.show()
        },
        error: function(error) {
            console.log(error);
        }
    });
}

function getStatus() {
    $.ajax({
        url: "/fsearch/api/status",
        type: "GET",
        success: function(response) {
            let json = JSON.parse(response);
            $("#statusMessage").text(json.message);
            $("#operatorBtn").off("click");
            if (json.status == "Running") {
                $("#statusBtn").html('<img src="images/doing.gif" class="rounded me-2" alt="...">');
                $("#operatorBtn").html('<i class="bi bi-stop-fill"></i>');
                $("#operatorBtn").on("click", function(event) {
                    event.preventDefault();
                    stopScan();
                });
            } else if (json.status == "Stopped") {
                $("#statusBtn").html('<i class="bi bi-stop-fill"></i>');
                $("#operatorBtn").html('<i class="bi bi-play-fill"></i>');
                $("#operatorBtn").on("click", function(event) {
                    event.preventDefault();
                    startScan();
                });
            }
            setTimeout(function() { getStatus() }, 1000);
        },
        error: function(error) {
            console.log(error);
        }
    });
}

function formatFileSize(size) {
    if (size < 1024) {
        return `${size}B`;
    } else if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(2)}KB`;
    } else if (size < 1024 * 1024 * 1024) {
        return `${(size / (1024 * 1024)).toFixed(2)}MB`;
    } else {
        return `${(size / (1024 * 1024 * 1024)).toFixed(2)}GB`;
    }
}

function base64Encode(str) {
    return btoa(unescape(str));
}


function getFileExtension(filename) {
    return filename.slice((filename.lastIndexOf(".") - 1 >>> 0) + 2);
}


function updateResultList(response) {
    let resultList = $("#resultList");
    resultList.empty();
    let ret = JSON.parse(response);
    if (ret.result == "ok") {
        ret.data.forEach(file => {
            let col = $("<div class='col-md-6'></div>");
            let card = $("<div class='card'></div>");
            let win_url = new URL(window.location.href);
            let downloadurl = new URL("/fsearch/api/download/" + file.doc + "/" + file.name, win_url).toString();
            let previeUrl = "/fsearch/onlinePreview?officePreviewType=pdf&url=" + base64Encode(downloadurl);
            if (file.size > 1024 * 1024 * 200) {
                previeUrl = "/fsearch/file-search/large.html";
            }
            let url = $("<a class='file-url-class' href='' target='_blank'></a>").attr("data", file.doc).attr("durl", downloadurl).attr("href", previeUrl).text(file.name);
            let downUrl = $("<a class='bi bi-cloud-download-fill' href='' target='_blank'></a>").attr("href", downloadurl);
            let header = $("<div class='card-header'></div>");
            let row = $('<div class="row"></div>');
            let header_col = $('<div class="col-sm-6"></div>');
            let down_col = $('<div class="col-sm-1"></div>');

            row.append(header_col);
            row.append(down_col);

            let size_col = $('<div class="col-sm-2" style="font-size:11px"></div>').text(formatFileSize(file.size));
            row.append(size_col);


            let time_col = $('<div class="col-sm-3" style="font-size:11px"></div>').text(formatDate(file.time * 1));
            row.append(time_col);

            header_col.append(url);
            down_col.append(downUrl);


            header.append(row);
            card.append(header);
            let cardBody = $("<div class='card-body'></div>");
            let path = file.path;
            let queryType = $("#queryType").val();
            if (queryType.indexOf('f') >= 0) {
                var arr = ret.keys;
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i].length > 0) {
                        path = path.replaceAll(arr[i], "<B><font color='red'>" + arr[i] + "</font></B>");
                    }

                }
            }

            
			// 使用正则表达式替换前缀，并将所有 '/' 替换为 '\'
            //let wk_path = path.replace(/^\/fsearch\/files\/volume[12]\//, '\\\\NAS\\').replace(/\//g, '\\');
            let wk_path = path.replace(/^\/fsearch\/files\/volume[12]\//, '\\\\NAS\\');
            wk_path = wk_path.replace(/<[^>]*>/g, match => match.replace(/\//g, '\x01')); // 暂时替换 / 为特殊字符
            wk_path = wk_path.replace(/\//g, '\\'); // 替换路径中的 /
            wk_path = wk_path.replace(/\x01/g, '/'); // 还原 <...> 内的 /
			//以上步骤的作用是避免搜索结果中的文本被替换
            let wk_path2 = wk_path.substring(0, wk_path.lastIndexOf('\\'));
             

            wk_path="<a style='color: gray; font-size: 0.9em; text-decoration: none;' ondblclick=\"var input=document.createElement('input');input.value=this.href;document.body.appendChild(input);input.select();document.execCommand('copy');document.body.removeChild(input);alert('链接已复制(因为网页中不支持访问本地路径): ' + this.href);\" href='"+wk_path2+"' >"+wk_path+"</a>";
            cardBody.append($("<p class='card-text'></p>").html(wk_path));
            if (file.match) {
                cardBody.append($("<p class='card-text'></p>").html(file.match));
            }
            card.append(cardBody);
            let footer = $("");
            col.append(card);
            resultList.append(col);

        });
        $(".file-url-class").off("click");
        $(".file-url-class").click(function(event) {
            event.preventDefault();
            $('#fileContent').text("");
            $('#pills-profile').html("");
            let doc = $(event.target).attr("data");
            let href = $(event.target).attr("href");
            let durl = $(event.target).attr("durl");
            $('#modelDownloadA').attr("href", durl);
            let text = $(event.target).text();
            $('#fileTitle').text(text);
            $.ajax({
                url: "/fsearch/api/get/" + doc,
                type: "GET",
                success: function(response) {
                    var json = JSON.parse(response);
                    $('#fileContent').text("因执行效率原因，此处已经关闭全文检索\r\n"+json.data.content);
                },
                error: function(error) {
                    console.log(error);

                }
            });

            bootstrap.Tab.getInstance($("#pills-home-tab")).show()
            $('#filePreview').modal('show');
            let iframe = '<iframe id="fileIframe" src="' + href + '" style="width:100%;height:100%;border:0;min-height:450px"></iframe>'
            $('#pills-profile').html(iframe);
        });
        updatePagination(ret.page, ret.count);
    }

}

$(document).ready(function() {
    getStatus();
    searchFile();
    $("#queryBtn").on("click", function(event) {
        event.preventDefault();
        searchFile();
    });
    $("#queryType").on("change", function(event) {
        event.preventDefault();
        searchFile();
    });

    $("#queryOrder").on("change", function(event) {
        event.preventDefault();
        searchFile();
    });

    $("#pageSize").on("change", function(event) {
        event.preventDefault();
        searchFile();
    });

    $('#query').keydown(function(event) {

        if (event.keyCode == 13) {
            event.preventDefault();
            searchFile(); //处理事件
        }
    });
});
