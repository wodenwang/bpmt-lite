/**
 * 核心js类
 * 
 * @author wodenwang
 */
var Ui = {

	/**
	 * 区域展示信息
	 * 
	 * @param {}
	 *            zone
	 * @param {}
	 *            msg
	 * @param {}
	 *            style info/warning/error
	 */
	msg : function(zone, msg, style) {
		var $zone;
		if (typeof (zone) == 'string') {
			$zone = $('#' + zone);
		} else {
			$zone = zone;
		}
		if (style == undefined || style == null) {
			style = 'info';
		}
		var closeText = '关闭';
		if (typeof (_lan) != "undefined" && _lan == 'en') {
			closeText = 'Close';
		}

		var role = style == 'error' || style == 'warning' ? 'alert' : 'status';
		var live = role == 'alert' ? 'assertive' : 'polite';
		var $div = $('<div class="ws-msg" style="position: relative;" role="' + role + '" aria-live="' + live + '">' + msg
				+ '<a href="javascript:void(0);" class="ui-corner-all msgPage" title="' + closeText + '" aria-label="' + closeText + '"><span class="ui-icon ui-icon-closethick">close</span></a></div>');
		$div.styleMsg({
			type : style
		});
		$zone.html('').append($div);

		$('a', $div).click(function(event) {
			event.preventDefault();
			$div.fadeOut(500);
		}).hover(function() {
			$(this).toggleClass('ui-state-hover');
		});
	},

	/**
	 * 警告框
	 * 
	 * @param msg
	 *            提示信息
	 */
	alert : function(msg) {
		var container = $('#ws-dialog-alert-div');
		if (!container || container.length > 0) {
			container.remove();
		}
		var title = "提示窗口";
		var btnText = '确定';
		if (typeof (_lan) != "undefined" && _lan == 'en') {
			title = 'Alert';
			btnText = 'OK';
		}

		$('body').append('<div id="ws-dialog-alert-div" style="display:none;" role="alert" aria-live="assertive">' + msg + '</div>');
		$("#ws-dialog-alert-div").styleMsg({
			type : 'info'
		});
		$("#ws-dialog-alert-div").dialog({
			title : title,
			modal : true,
			buttons : [ {
				icons : {
					primary : "ui-icon-alert"
				},
				text : btnText,
				click : function() {
					$(this).dialog("close");
				}
			} ]
		});
	},

	/**
	 * 询问框
	 * 
	 * @param msg
	 *            提示信息
	 * @param fn
	 *            点击OK的回调函数
	 * @param cancelFn
	 *            点击取消的回调函数
	 */
	confirm : function(msg, fn, cancelFn) {
		var container = $('#ws-dialog-confirm-div');
		if (!container || container.length > 0) {
			container.remove();
		}
		var title = "提示窗口";
		var okText = "确定";
		var cancelText = "取消";
		if (typeof (_lan) != "undefined" && _lan == 'en') {
			title = "Warnning";
			okText = "OK";
			cancelText = "Cancel";
		}

		$('body').append('<div id="ws-dialog-confirm-div" style="display:none;" role="alertdialog" aria-live="assertive">' + msg + '</div>');
		$("#ws-dialog-confirm-div").styleMsg({
			type : 'warning'
		});
		$("#ws-dialog-confirm-div").dialog({
			title : title,
			modal : true,
			buttons : [ {
				icons : {
					primary : "ui-icon-alert"
				},
				text : okText,
				click : function() {
					$(this).dialog("close");
					fn();
				}
			}, {
				icons : {
					primary : "ui-icon-cancel"
				},
				text : cancelText,
				click : function() {
					$(this).dialog("close");
					if ($.isFunction(cancelFn)) {
						cancelFn();
					}
				}
			} ]
		});
	},

	/**
	 * 获取当前页面的tab框架
	 * 
	 * @return {}
	 */
	getTab : function() {
		var $tab = $('div[tabs=true][main=true]:first');
		if ($tab && $tab.size() > 0) {
			return $tab;
		} else
			throw new Error("编程出错,找不到tab");
	},

	/**
	 * 新建一个tab
	 * 
	 * @param {}
	 *            title
	 * @param {}
	 *            url
	 */
	openTab : function(title, url) {
		return Ajax.tab(Ui.getTab(), url, {
			title : title
		});
	},

	/**
	 * 新建窗口
	 * 
	 * @param {}
	 *            title
	 * @param {}
	 *            url
	 * @param {}
	 *            width
	 * @param {}
	 *            height
	 */
	openWin : function(title, url, width, height) {
		if (!width) {
			width = 1024;
		}
		if (!height) {
			height = 500;
		}
		return Ajax.win(url, {
			title : title,
			minWidth : width,
			minHeight : height
		});
	},

	/**
	 * 关闭当前tab
	 * 
	 * @param {}
	 *            tab
	 */
	closeTab : function(tab) {
		var tabid;
		if (typeof (tab) == 'string') {
			tabid = tab;
		} else {
			try {
				tabid = tab.attr("id");
			} catch (e) {
				tabid = tab.id;
			}
		}
		$('li[aria-controls=' + tabid + '] span').click();
		// $tab.tabs("option", "active", 0);
	},

	/**
	 * 关闭当前tab/win
	 * 
	 * @param {}
	 *            zone
	 */
	closeCurrent : function(zone) {
		var $this;
		if (typeof (zone) == 'string') {
			$this = $('#' + zone);
		} else {
			try {
				zone.attr('id');
				$this = zone;
			} catch (e) {
				$this = $(zone);
			}
		}

		if ($this.attr('tab') == 'true') {
			Ui.closeTab($this);
		} else if ($this.attr('win') == 'true') {
			$this.dialog("close");
		} else {
			window.close();
			$this.html('');
			var $msg = $('<div>您的浏览器不支持自动关闭窗口,请手动关闭.</div>').styleMsg({
				type : 'warning'
			});
			$this.append($msg);
		}
	},

	/**
	 * 修改当前tab/win的标题
	 * 
	 * @param {}
	 *            zone
	 * @param {}
	 *            title
	 */
	changeCurrentTitle : function(zone, title) {
		var $this;
		if (typeof (zone) == 'string') {
			$this = $('#' + zone);
		} else {
			try {
				zone.attr('id');
				$this = zone;
			} catch (e) {
				$this = $(zone);
			}
		}

		try {
			if ($this.attr('tab') != undefined) {
				$('li[aria-controls=' + $this.attr('id') + '] a').html(title);
			} else if ($this.attr('win') == 'true') {
				$this.parent().find('.ui-dialog-title:first').html(title);
			}
		} catch (e) {
			// do nothing
		}
	},

	/**
	 * 获取当前tab/win的标题
	 * 
	 * @param {}
	 *            zone
	 * @return {}
	 */
	getCurrentTitle : function(zone) {
		var $this;
		if (typeof (zone) == 'string') {
			$this = $('#' + zone);
		} else {
			try {
				zone.attr('id');
				$this = zone;
			} catch (e) {
				$this = $(zone);
			}
		}

		try {
			if ($this.attr('tab') != undefined) {
				return $('li[aria-controls=' + $this.attr('id') + '] a').html();
			} else if ($this.attr('win') == 'true') {
				return $this.dialog("option", "title");
			}
		} catch (e) {
			return '';
		}
	},

	/**
	 * 新建标签
	 * 
	 * @param {}
	 *            tabs
	 * @param {}
	 *            title
	 * @param {}
	 *            $content
	 */
	newTab : function(tabs, title, $content) {
		if (typeof (tabs) == 'string') {
			tabs = $('#' + tabs);
		}
		var currentSize = $('li[aria-selected]', $('ul:first', tabs)).size();
		var maxSize = tabs.attr('max');
		if (maxSize == null || maxSize == '') {
			maxSize = 100;
		}
		if (currentSize >= maxSize) {
			alert('当天标签数已超过最大值[' + maxSize + ']，请先关闭一些标签再操作。');
			return;
		}

		var closeText = '关闭';
		var id = 'tab_' + Core.nextSeq();
		var $li = $('<li><a href="#' + id + '">' + title + '</a></li>');
		var $span = $('<span class="ui-icon ui-icon-close" role="presentation" style=" float: right; margin: 0.4em 0.2em 0 0; cursor: pointer;">' + closeText + '</span>');
		$span.click(function() {
			var panelId = $(this).closest("li").remove().attr("aria-controls");
			$("#" + panelId).remove();
			tabs.tabs("refresh");
		});
		$li.append($span);
		$("ul.ui-tabs-nav:first", tabs).append($li);
		var $div = $("<div id='" + id + "'></div>");
		$div.append($content);
		tabs.append($div);

		var btn = tabs.attr("button");
		if (btn && btn == 'left') {
			tabs.tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
			$('li', tabs).removeClass("ui-corner-top").addClass("ui-corner-left");
		}

		tabs.tabs("refresh");
		tabs.tabs("option", "active", currentSize);

	}
};
