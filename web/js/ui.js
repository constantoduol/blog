var dom = {
    /**
     *
     * returns an element with the specified id
     */
    el: function (id) {
        var element = document.getElementById(id);
        if (element) {
            element.attr = function (name, value) {
                this.setAttribute(name, value);
            };
            element.add = function (elem) {
                this.appendChild(elem);
            };
        }
        return element;
    },
    /**
     *creates a new element with the specified tag name
     */
    newEl: function (tag) {
        var element = document.createElement(tag);
        element.attr = function (name, value) {
            this.setAttribute(name, value);
        };
        element.add = function (elem) {
            this.appendChild(elem);
        };
        return element;
    },
    /**
     *
     * checks whether the document has completely loaded
     */
    ready: function () {
        if (document.readyState === "complete") {
            return true;
        }
        else {
            return false;
        }
    },
    /**
     * 
     * returns an array of elements with the specified tag name
     *
     */
    tags: function (name) {
        return document.getElementsByTagName(name);
    },
    /**
     *waits till the document is ready and then executes the function func
     */
    waitTillReady: function (func) {
        var time = setInterval(function () {
            if (dom.ready()) {
                clearInterval(time);
                func();
            }
        }, 5);

    },
    /**
     *waits till the document is ready and then executes the function func
     */
    waitTillElementReady: function (id, func) {
        var time = setInterval(function () {
            if (dom.el(id)) {
                clearInterval(time);
                func();
            }
        }, 5);

    }



}

var funcs = {} // namespace for functions



window.cookieStorage = {
    getItem: function (sKey) {
        if (!sKey || !this.hasOwnProperty(sKey)) {
            return null;
        }
        return unescape(document.cookie.replace(new RegExp("(?:^|.*;\\s*)" + escape(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=\\s*((?:[^;](?!;))*[^;]?).*"), "$1"));
    },
    key: function (nKeyId) {
        return unescape(document.cookie.replace(/\s*\=(?:.(?!;))*$/, "").split(/\s*\=(?:[^;](?!;))*[^;]?;\s*/)[nKeyId]);
    },
    setItem: function (sKey, sValue) {
        if (!sKey) {
            return;
        }
        document.cookie = escape(sKey) + "=" + escape(sValue) + "; expires=Tue, 19 Jan 2038 03:14:07 GMT; path=/";
        this.length = document.cookie.match(/\=/g).length;
    },
    length: 0,
    removeItem: function (sKey) {
        if (!sKey || !this.hasOwnProperty(sKey)) {
            return;
        }
        document.cookie = escape(sKey) + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
        this.length--;
    },
    hasOwnProperty: function (sKey) {
        return (new RegExp("(?:^|;\\s*)" + escape(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=")).test(document.cookie);
    }
};
window.cookieStorage.length = (document.cookie.match(/\=/g) || window.cookieStorage).length;



var ui = {
    /*
     *  ui appends the input
     *
     * value
     * onclick
     * class
     * other attributes
     * 
     */
    resizables: [],
    resizableIds: [],
    element: function (attr) {
        var run = function (id) {
            var com = dom.newEl(attr.tag);
            com.attr("id", id);
            delete attr.tag;
            for (var param in attr) {
                if (typeof attr[param] == "function") {
                    var func = attr[param];
                    var str = func.toString();
                    var funcBody = str.substring(str.indexOf("("));
                    var funcName = "function_" + Math.floor(Math.random() * 100000000) + " ";
                    var newFunc = "funcs." + funcName + "= function " + funcName + funcBody;
                    eval(newFunc);
                    com.attr(param, "funcs." + funcName + "()");
                }
                else if (param == "klass") {
                    com.attr("class", attr[param]);
                }
                else if (param == "content") {
                    com.innerHTML = attr[param];
                }
                else {
                    com.attr(param, attr[param]);
                }
            }
            if (attr.parent) {
                //append this element to the specified parent
                if (typeof attr.parent == "string") {
                    dom.el(attr.parent).appendChild(com);
                }
                else {
                    attr.parent.appendChild(com);
                }
                delete attr.parent;
            }
            else {
                dom.tags("body")[0].appendChild(com);
            }
            return com;
        }

        function wait(func, id) {
            var time = setInterval(function () {
                if (dom.ready()) {
                    clearInterval(time);
                    func(id);
                }
            }, 5);
        }
        if (!attr.id) {
            // bind this id if none is specified
            attr.id = "element_" + Math.floor(Math.random() * 1000000);
        }
        wait(run, attr.id); // run this function only after the document is ready
        return attr.id;
    },
    dropdown: {
        add: function (idToAppend, elementId, arrOptions, arrValues) {
            var func = function () {
                var contDiv = dom.newEl("div");
                contDiv.attr("class", "btn-group");
                var aLabel = dom.newEl("a");
                aLabel.attr("class", "btn");
                aLabel.attr("href", "#");
                aLabel.innerHTML = arrOptions[0];
                aLabel.attr("id", elementId + "_root");
                aLabel.attr("value", arrValues[0]);
                var aCaret = dom.newEl("a");
                aCaret.attr("class", "btn dropdown-toggle");
                aCaret.attr("data-toggle", "dropdown");
                aCaret.attr("href", "#");

                var aSpan = dom.newEl("span");
                aSpan.attr("class", "caret");
                aCaret.add(aSpan);

                contDiv.add(aLabel);
                contDiv.add(aCaret);

                var ul = dom.newEl("ul");
                ul.attr("id", elementId);
                ul.attr("class", "dropdown-menu");
                ul.attr("style", "text-align : left");
                contDiv.add(ul);
                for (var x = 0; x < arrOptions.length; x++) {
                    var li = dom.newEl("li");
                    li.attr("value", arrValues[x]);
                    li.attr("option", arrOptions[x]);
                    var liDiv = dom.newEl("li");
                    liDiv.attr("class", "divider");
                    var a = dom.newEl("a");
                    a.href = "#";
                    a.attr("onclick", "ui.dropdown.setSelected(\"" + elementId + "\",\"" + arrValues[x] + "\")");
                    a.innerHTML = arrOptions[x];
                    li.add(a);
                    ul.add(li);
                    ul.add(liDiv);
                }
                dom.el(idToAppend).add(contDiv);
            };
            dom.waitTillElementReady(idToAppend, func);
        },
        getSelected: function (id) {
            return dom.el(id + "_root").getAttribute("value");
        },
        setSelected: function (id, value) {
            var root = dom.el(id);
            for (var x = 0; x < root.children.length; x++) {
                var li = root.children[x];
                var val = li.getAttribute("value");
                if (val === value) {
                    var option = li.getAttribute("option");
                    var rootLi = dom.el(id + "_root");
                    rootLi.attr("value", value);
                    rootLi.innerHTML = option;
                    break;
                }
            }

        }
    },
    modal: function (title, uiFunc, funcOk, funcCancel) {
        var window = dom.el("alert-window");
        if (!window) {
            var html = "<div class='modal' id='alert-window'>" +
                    "<div class='modal-dialog'>" +
                    "<div class='modal-content'>" +
                    "<div class='modal-header'>" +
                    "<h4 class='modal-title' id='modal-title'></h4>" +
                    "</div>" +
                    "<div class='modal-body' id='modal-content'>" +
                    "</div>" +
                    "<div class='modal-footer'>" +
                    "<button type='button' class='btn btn-default' data-dismiss='modal' id='cancel_func'>Cancel</button>" +
                    "<button type='button' class='btn btn-primary' id='ok_func'>OK</button>" +
                    "</div>" +
                    "</div><!-- /.modal-content -->" +
                    "</div><!-- /.modal-dialog -->" +
                    "</div><!-- /.modal -->";
            $("body").append(html);
        }
        $("#modal-content").html("");
        $("#modal-title").html(title);
        eval(uiFunc);
        $("#ok_func").attr("onclick", funcOk);
        $("#cancel_func").attr("onclick", funcCancel);
        $("#alert-window").modal();
    },
    staticmodal: function (areaToAppend, title, uiFunc) {
        var id = Math.floor(Math.random() * 100000000);
        var html = "<div class='static-modal'>" +
                "<div class='modal'>" +
                "<div class='modal-dialog'>" +
                "<div class='modal-content'>" +
                "<div class='modal-header'>" +
                "<h3 class='modal-title' id='modal_title'></h3>" +
                "</div>" +
                "<div class='modal-body' id='modal_body'>" +
                "</div>" +
                "</div><!-- /.modal-content -->" +
                "</div><!-- /.modal-dialog -->" +
                "</div><!-- /.modal -->" +
                "</div>";
        $("#" + areaToAppend).append(html);
        $("#modal_title").html(title);
        eval(uiFunc);
        $("#modal_title").attr("id", "");
        var modalBody = $("#modal_body");
        modalBody.attr("id", id);
        var factor = isMobile() ? 1 : 0.87;
        var res = {id: id, width : factor};
        ui.resizable(res);
        ui.resize();
        return id;
    },
    mobilecheck: function () {
        var check = false;
        (function (a) {
            if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4)))
                check = true
        })(navigator.userAgent || navigator.vendor || window.opera);
        return check;
    },
    getDim: function () {
        var body = window.document.body;
        var screenHeight;
        var screenWidth;
        if (window.innerHeight) {
            screenHeight = window.innerHeight;
            screenWidth = window.innerWidth;
        } else if (body.parentElement.clientHeight) {
            screenHeight = body.parentElement.clientHeight;
            screenWidth = body.parentElement.clientWidth;
        } else if (body && body.clientHeight) {
            screenHeight = body.clientHeight;
            screenWidth = body.clientWidth;
        }
        return [screenWidth, screenHeight];
    },
    resizable: function (resizable) {
        if (ui.resizableIds.indexOf(resizable.id) > -1)
            return;
        ui.resizables.push(resizable);
    },
    resize: function () {
        var arr = ui.resizables;
        for (var index in arr) {
            var obj = arr[index];
            var element = dom.el(obj.id);
            if (!element) {
                console.log("Element " + obj.id + " not found!");
                continue;
            }
            element.style.width = ui.getDim()[0] * obj.width + "px";
            element.style.height = ui.getDim()[1] * obj.height + "px";
            if (obj.style) {
                for (var style in obj.style) {
                    var factor = obj.style[style].factor;
                    var along = obj.style[style].along;
                    if (along === "height") {
                        element.style[style] = factor * game.getDim()[1] + "px";
                    }
                    else if (along === "width") {
                        element.style[style] = factor * game.getDim()[0] + "px";
                    }
                }
            }

        }

    }

};







var ajax = {
    /**
     *@param data the data to be pushed to the server
     */
    run: function (data) {
        /*
         * url
         * loadArea
         * data
         * type
         * success
         * error
         */

        //show that this page is loading
        if (data.loadArea) {
            dom.el(data.loadArea).style.display = "block";
        }
        function callback(xhr) {
            return function () {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        var resp = xhr.responseText;
                        var json = JSON.parse(resp);
                        if (data.success !== null) {
                            if (data.loadArea) {
                                dom.el(data.loadArea).style.display = "none";
                            }
                            if (json.type === "auth_url") {
                                window.location = json.data;
                            }
                            else {
                                data.success(json);
                            }
                        }

                    } else {
                        if (data.error !== null) {
                            if (data.loadArea) {
                                dom.el(data.loadArea).style.display = "none";
                            }
                            data.error(data);
                        }
                    }
                }
            }
        }

        return function () {
            var xhr = getRequestObject();
            if (data.error != null) {
                if (xhr.onerror) {
                    xhr.onerror = data.error;
                }
            }
            sendJSON(data.url, data.data, xhr, callback(xhr), data.type);
        }();

    }



};










/*
 * this function returns an xml http object
 */

function getRequestObject() {
    if (window.ActiveXObject) {
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
    else if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
    }
    else {
        return null;
    }

}






/**
 * used to send json data to the server
 * @param serverUrl the url to the server where the data is to be sent
 * @param json this is the json data to be sent to the server
 * @param request this is the xmlhttp request object
 * @param callback this is the callback function
 * @param type post or get
 */
function sendJSON(serverUrl, json, request, callback, type) {
    json = "json=" + JSON.stringify(json);
    request.onreadystatechange = callback
    request.open(type, serverUrl, true);
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.send(json);
}





