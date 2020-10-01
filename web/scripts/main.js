var mars = (function () {
    return {

        init: function () {
            // register event listener
            document.getElementById('avatar').addEventListener('click', function () {
                UIController.showLoginForm();
            });
            document.getElementById("authenticate").addEventListener('click', function () {
                userController.loginUser();
            });
            document.getElementById("password").addEventListener('keypress', function (e) {
                if (e.key === 'Enter') {
                    userController.loginUser();
                }
            });
            document.getElementById("deauthenticate").addEventListener('click', function () {
                userController.logoutUser();
            });
            document.getElementById('nearby-btn').addEventListener('click', function () {
                itemController.loadNearbyItems();
            });
            document.getElementById('fav-btn').addEventListener('click', function () {
                itemController.loadFavoriteItems();
            });
            document.getElementById('recommend-btn').addEventListener('click', function () {
                itemController.loadRecommendedItems();
            });


            var welcomeMsg = document.getElementById('welcome-msg');
            welcomeMsg.innerText = 'Welcome';
            UIController.refreshProfile();
            itemController.initGeoLocation();
        },
    }
})();

var UIController = (function () {

    function createElement(tag, options) {
        var element = document.createElement(tag);
        for (var option in options) {
            if (options.hasOwnProperty(option)) {
                element[option] = options[option];
            }
        }
        return element;
    }

    function changeFavoriteItem(item_id) {

        var li = document.getElementById('item-' + item_id);
        var favIcon = document.getElementById('fav-icon-' + item_id);
        var favorite = li.dataset.favorite !== 'true';

        var url = './history'
        var req = JSON.stringify({
            userId: "user_id",
            favorites: [item_id]
        });

        var method = favorite ? 'POST' : 'DELETE';
        ajax(method, url, req, function (res) {
            var result = JSON.parse(res);
            if (result.status === 'SUCCESS') {
                li.dataset.favorite = favorite;
                favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
            }
        });
    }

    function addItem(itemList, item) {
        var item_id = item.id;

        var li = createElement('li',
            {
                id: 'item-' + item_id,
                className: 'item'
            })

        li.dataset.item_id = item_id;
        li.dataset.favorite = item.favorite;

        // append image to li
        if (item.imageUrl) {
            li.appendChild(createElement('img', {src: item.imageUrl}));
        } else {
            li.appendChild(createElement('img', {src: ''}))
        }

        // append div to li
        var section = createElement('div', {});

        // title
        var title = createElement('a',
            {
                href: item.url,
                target: '_blank',
                className: 'item-name'
            });
        title.innerHTML = item.name;
        section.appendChild(title);


        var category = createElement('p', {className: 'item-category'});
        category.innerHTML = 'Category: ' + item.categories.join(', ');
        section.appendChild(category);


        var stars = createElement('div', {className: 'stars'});
        for(var i = 0; i < item.rating; i++) {
            var star = createElement('i', {className: 'fa fa-star'});
            stars.appendChild(star);
        }
        if (('' + item.rating).match(/\.5$/)) {
            stars.appendChild(createElement('i', {className: 'fa fa-star-half-o'}));
        }
        section.appendChild(stars);

        li.appendChild(section);


        var address = createElement('p', {className: 'item-address'});
        address.innerHTML = item.address.replace(/,/g,'<br/>').replace(/\"/g,'');
        li.appendChild(address);


        var favLink = createElement('p', {className: 'fav-link'});
        favLink.onclick = function () {
            changeFavoriteItem(item_id);
        };

        favLink.appendChild(createElement('i', {
            id: 'fav-icon-' + item_id,
            className: item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
        }));

        li.appendChild(favLink);

        itemList.appendChild(li);

    }

    return {
        showLoginForm: function () {
            var username = getCookie("username");
            document.getElementById("password").placeholder = "Demo Password: ryan";
            if (username !== null && username !== undefined) {
                document.getElementById('login').style.display='none';
                document.getElementById('logout').style.display='block';
            } else {
                document.getElementById('logout').style.display='none';
                document.getElementById('login').style.display='block';
            }
        },
        closeLoginForm: function () {
            document.getElementById('login').style.display='none';
            document.getElementById("password").value = "";
        },
        closeLogoutForm: function () {
            document.getElementById('logout').style.display='none';
        },
        activeBtn: function(btnId) {
            var btns = document.getElementsByClassName('main-nav-btn');

            for (var i = 0; i < btns.length; i++) {
                btns[i].className = btns[i].className.replace(/\bactive\b/, '');
            }

            var btn = document.getElementById(btnId);
            btn.className += ' active';
        },
        showLoadingMessage: function(msg) {
            var itemList = document.getElementById('item-list');
            itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i>' + msg + '</p>';
        },
        showWarningMessage: function(msg) {
            var itemList = document.getElementById('item-list');
            itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i>' + msg + '</p>';
        },
        showErrorMessage: function(msg) {
            var itemList = document.getElementById('item-list');
            itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i>' + msg + '</p>';
        },
        listItems: function(items) {
            var itemList = document.getElementById('item-list');
            itemList.innerHTML = '';

            for (var i = 0; i < items.length; i++) {
                addItem(itemList, items[i]);
            }
        },
        refreshProfile: function () {
            var username = getCookie("username");
            if (username !== null && username !== undefined) {
                var welcomeMsg = document.getElementById('welcome-msg');
                welcomeMsg.innerText = 'Welcome, ' + username;
            }
        },
        reloadInfo: function () {
            // get active button
            var currentActiveBtn = document.getElementsByClassName("main-nav-btn active")[0].id;
            if (currentActiveBtn === "fav-btn"){
                itemController.loadFavoriteItems();
            } else if (currentActiveBtn === "recommend-btn"){
                itemController.loadRecommendedItems();
            } else {
                itemController.loadNearbyItems();
            }
        },

    }
})();


var userController = (function () {

    var welcomeMsg = document.getElementById('welcome-msg');

    return {
        loginUser: function () {

            var username = document.getElementById("username").value;
            var password = document.getElementById("password").value;

            document.getElementById("password").value = "";

            var url = './login';
            var req = JSON.stringify({
                username: username,
                password: password
            });

            ajax('POST', url, req, function (res) {
                var token = JSON.parse(res);
                if ('jwt' in token) {
                    password = "";
                    // document.cookie= "username=" + username;
                    // document.cookie= "jwt=" + token.jwt;
                    setCookie("username", username);
                    setCookie("jwt", token.jwt);

                    UIController.closeLoginForm();
                    UIController.refreshProfile();
                    UIController.reloadInfo();

                } else {
                    document.getElementById("password").value = null;
                    document.getElementById("password").placeholder = "Invalid Password";
                }
            });
        },

        logoutUser: function () {
            welcomeMsg.innerText = 'Welcome';
            deleteCookie("username");
            deleteCookie("jwt");
            UIController.closeLogoutForm();
            UIController.refreshProfile();
            UIController.reloadInfo();
        }
    }
})();


var itemController = (function () {

    var lat = 37.1;
    var lon = -120;

    function onPositionUpdated(position) {
        lat = position.coords.latitude;
        lon = position.coords.longitude;

        itemController.loadNearbyItems();
    }

    function onLoadPositionFailed() {
        console.log("navigator.geolocation is not available");

        getLocationFromIP();
    }

    function getLocationFromIP() {
        var url = 'http://ipinfo.io/json?token=1234567890';
        var req = null;

        ajaxExternal("GET", url, req, function (res) {
            var result = JSON.parse(res);
            if ('loc' in result) {
                var loc = result.loc.split(',');
                lat = loc[0];
                lon = loc[1];
            } else {
                console.warn('Getting location by IP failed')
            }

            itemController.loadNearbyItems();
        });
    }



    return {
        initGeoLocation: function() {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(onPositionUpdated,
                    onLoadPositionFailed,
                    {maximumAge: 60000})
                UIController.showLoadingMessage('Retrieving your location');
            } else {
                onLoadPositionFailed();
            }
        },
        loadNearbyItems: function() {
            console.log('loadNearbyItems');

            UIController.activeBtn('nearby-btn');

            var url = './search';
            var params = 'lat=' + lat + '&lon=' + lon;
            var req = JSON.stringify({});

            UIController.showLoadingMessage('Loading nearby items...');

            ajax('GET', url + '?' + params, req,
                function (res) {
                    var items = JSON.parse(res);
                    if (!items || items.length === 0) {
                        UIController.showWarningMessage('No nearby item.');
                    } else {
                        UIController.listItems(items);
                    }
                },
                function () {
                    UIController.showErrorMessage('Cannot load nearby items');
                });
        },

        loadFavoriteItems: function() {
            console.log('loadFavoriteItems');
            UIController.activeBtn('fav-btn');

            var url = './history';
            var req = JSON.stringify({});

            UIController.showLoadingMessage('Loading favorite items...');

            ajax('GET', url, req,
                function (res) {
                    var items = JSON.parse(res);
                    if (!items || items.length === 0) {
                        UIController.showWarningMessage('No favorite item.');
                    } else {
                        UIController.listItems(items);
                    }
                },
                function () {
                    UIController.showErrorMessage('Cannot load favorite items');
                });
        },

        loadRecommendedItems: function() {
            console.log('loadRecommendedItems');
            UIController.activeBtn('recommend-btn');

            var url = './recommendation';
            var params = 'lat=' + lat + '&lon=' + lon;
            var req = JSON.stringify({});

            UIController.showLoadingMessage('Loading recommended items...');

            ajax('GET', url + '?' + params, req,
                function (res) {
                    var items = JSON.parse(res);
                    if (!items || items.length === 0) {
                        UIController.showWarningMessage('No recommended item.');
                    } else {
                        UIController.listItems(items);
                    }
                },
                function () {
                    UIController.showErrorMessage('Cannot load recommended items');
                });
        }


    }
})();


function ajax(method, url, data, callback, errorHandler) {
    var xhr = new XMLHttpRequest();

    xhr.open(method, url, true);

    var token = getCookie("jwt");
    if (token !== null && token !== undefined) {
        xhr.setRequestHeader("Authorization", "Bearer " + token);
    }

    xhr.onload = function () {
        if (xhr.status === 200) {
            callback(xhr.responseText);
        } else if (xhr.status === 403) {
            onSessionInvalid();
        } else {
            errorHandler();
        }
    }

    xhr.onerror = function () {
        console.error("The request couldn't  be completed");
        errorHandler();
    }

    if (data === null) {
        xhr.send();
    } else {
        xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
        xhr.send(data);
    }
}

function ajaxExternal(method, url, data, callback, errorHandler) {
    var xhr = new XMLHttpRequest();

    xhr.open(method, url, true);

    xhr.onload = function () {
        if (xhr.status === 200) {
            callback(xhr.responseText);
        } else if (xhr.status === 403) {
            onSessionInvalid();
        } else {
            errorHandler();
        }
    }

    xhr.onerror = function () {
        console.error("The request couldn't  be completed");
        errorHandler();
    }

    if (data === null) {
        xhr.send();
    } else {
        xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
        xhr.send(data);
    }
}

function onSessionInvalid() {
    UIController.showErrorMessage("Need to login to visit this page");
}

function setCookie(name, value, options = {}) {

    options = {
        path: '/',
        // add other defaults here if necessary
        ...options
    };

    if (options.expires instanceof Date) {
        options.expires = options.expires.toUTCString();
    }

    let updatedCookie = encodeURIComponent(name) + "=" + encodeURIComponent(value);

    for (let optionKey in options) {
        updatedCookie += "; " + optionKey;
        let optionValue = options[optionKey];
        if (optionValue !== true) {
            updatedCookie += "=" + optionValue;
        }
    }

    document.cookie = updatedCookie;
}

function getCookie(name) {
    let matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

function deleteCookie(name) {
    setCookie(name, "", {
        'max-age': -1
    })
}

mars.init();