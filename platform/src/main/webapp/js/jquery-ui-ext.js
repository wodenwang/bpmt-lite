var _core = {

	_data : {
		_i : 0
	},

	/**
	 * 表格样式定义
	 */
	styleTable : function($) {
		$.fn.styleTable = function(options) {
			return this.each(function() {
				$this = $(this);
				$this.addClass('ui-styled-table');

				// 解决表格嵌套样式冲突
				var $parentTr = $this.parents('.ui-styled-table>tbody>tr:first');
				if ($parentTr.size() > 0) {
					var $parentHidden = $this.parents(':hidden', $parentTr);
					if ($parentHidden.size() <= 0) {
						$parentTr.unbind('mouseover');
						$parentTr.unbind('mouseout');
						$parentTr.children('td').removeClass("ui-state-hover");
						$this.parents('td:first').removeClass('left');
					}
				}

				$this.find('>tbody>tr').on('mouseover mouseout', function(event) {
					$(this).children('td').toggleClass("ui-state-hover", event.type == 'mouseover');
				});

				$this.find(">thead>tr>th").addClass("ui-widget-header");
				$this.find(">tbody>tr>th").addClass("ui-widget-header");
				$this.find(">tbody>tr>td").addClass("ui-widget-content");
				$this.find(">tbody>tr:last-child").addClass("last-child");
			});
		};
	},
	/**
	 * 分页控制条初始化
	 */
	styleBar : function($) {
		$.fn.styleBar = function(options) {
			var defaults = {
				css : 'ui-styled-bar'
			};
			options = $.extend(defaults, options);
			return this.each(function() {
				$this = $(this);
				$this.addClass(options.css);
				$this.addClass('ui-corner-all');
				$this.addClass('ui-widget-header');

				// 按钮组
				// $this.find(".ws-group").buttonset();
			});
		};
	},

	/**
	 * 按钮样式初始化
	 */
	styleButton : function($) {
		$.fn.styleButton = function(options) {
			return this.each(function() {
				var icon = $(this).attr("icon");
				var text = $(this).attr("text");
				var secicon = $(this).attr("secicon");
				var state = $(this).attr("state");
				if (state != undefined) {
					if (state == 'disabled') {
						$(this).prop("disabled", true);
					}
					if (state == 'readonly') {
						$(this).prop("readonly", true);
					}
				}

				if (text == 'false') {
					text = false;
				} else {
					text = true;
				}

				var png = null;
				if (icon && icon.indexOf('.') > 0) {
					png = icon;
					icon = 'system_';
				}

				var $button = $(this);
				var buttonText = $.trim($button.text());
				if (buttonText != '') {
					if (!$button.attr('title')) {
						$button.attr('title', buttonText);
					}
					if (!$button.attr('aria-label')) {
						$button.attr('aria-label', buttonText);
					}
				}
				$button.button({
					text : text,
					icons : {
						primary : icon != '' && icon != null ? ("ui-icon-" + icon) : null,
						secondary : secicon != '' && secicon != null ? ("ui-icon-" + secicon) : null
					}
				});
				if (png) {
					$('span.ui-icon-system_', $button).css({
						"background-image" : "url('/css/icon/" + png + "')",
						"background-position" : "0 0"
					});
				}
			});
		};
	},
	/**
	 * 消息提示界面初始化
	 */
	styleMsg : function($) {
		var types = {
			'normal' : {
				style : 'ui-state-default',
				icon : 'ui-icon-info',
				text : '提示：'
			},
			'info' : {
				style : 'ui-state-highlight',
				icon : 'ui-icon-info',
				text : '提示：'
			},
			'warning' : {
				style : 'ui-state-highlight',
				icon : 'ui-icon-alert',
				text : '警告：'
			},
			'error' : {
				style : 'ui-state-error',
				icon : 'ui-icon-circle-close',
				text : '错误：'
			}
		};
		
		if (typeof (_lan) != "undefined" && _lan == 'en') {
			types = {
					'normal' : {
						style : 'ui-state-default',
						icon : 'ui-icon-info',
						text : 'Cue：'
					},
					'info' : {
						style : 'ui-state-highlight',
						icon : 'ui-icon-info',
						text : 'Cue：'
					},
					'warning' : {
						style : 'ui-state-highlight',
						icon : 'ui-icon-alert',
						text : 'Warning：'
					},
					'error' : {
						style : 'ui-state-error',
						icon : 'ui-icon-circle-close',
						text : 'Error：'
					}
				};
		};
		
		
		$.fn.styleMsg = function(options) {
			var type = null;
			if (options && options != null) {
				type = types[options.type];
			}
			if (type == null) {
				type = types['info'];
			}

			return this.each(function() {
				$this = $(this);
				$this.addClass('ui-widget');
				$this.css('line-height', '18px');
				var msg = $this.html();
				var $frame = $('<div class="ui-corner-all ' + type.style + '" style="padding: 0 .7em;"></div>');
				var $p = $('<p></p>');
				var $text = $('<strong>' + type.text + '</strong>');
				var $icon = $('<span class="ui-icon ' + type.icon + '" style="float: left; margin-right: .3em;"></span>');
				$p.append($icon);
				$p.append($text);
				$p.append(msg);
				$frame.append($p);
				$this.html('');
				$this.append($frame);
			});
		};
	},

	/**
	 * 表格底部/顶部的bar
	 * 
	 * @param $
	 */
	styleTableBar : function($) {
		$.fn.styleTableBar = function(options) {
			return this.each(function() {
				$this = $(this);
				$this.addClass('ui-styled-bar');
				$this.attr("colspan", 500);
				// 按钮组
				// $this.find(".ws-group").buttonset();
			});
		};
	},

	/**
	 * radio/checkbox格式化
	 * 
	 * @param {} $
	 */
	radioset : function($) {
		$.fn.radioset = function(options) {
			return this.each(function() {
				var $this = $(this);
				var $label = $this.next('label');
				var id = $this.attr("id");
				if (id == undefined) {
					id = 'radio_' + Core.nextSeq();
					$this.attr("id", id);
				}
				if ($label.size() > 0) {
					$label.attr("for", id);

					// 配套label才渲染icheck
					var icheck = $this.attr('icheck');// 增加icheck标识,false则不渲染
					if (icheck == undefined || icheck != 'false') {
						$label.css('margin-left', '1px');
						$label.css('margin-right', '5px');
						$label.css('cursor', 'pointer');

						$this.iCheck({
							checkboxClass : 'icheckbox_square-blue',
							radioClass : 'iradio_square-blue',
							increaseArea : '20%'
						});
					}
				}
			});
		};
	},

	/**
	 * 图表
	 * 
	 * @param {} $
	 */
	charts : function($) {
		$.fn.charts = function(cmd) {

			if (cmd == undefined) {
				cmd = 'init';
			}

			// 没有引入echarts则不做任何处理
			if (typeof echarts == "undefined") {
				return this;
			}

			if (_core._data.charts == undefined) {
				_core._data.charts = {};
			}

			var _height = 400;// 默认高度
			var _min_width = 300;// 默认最小宽度

			return this.each(function() {
				var $this = $(this);

				if ($this.attr('id') == undefined || $this.attr('id') == '') {
					$this.attr('id', '_charts_' + (_core._data._i++));
				}
				var id = $this.attr('id');

				var myCharts = _core._data.charts[id];
				if (myCharts) {
					if (cmd == 'resize') {
						myCharts.resize();
						myCharts.restore();
					} else if (cmd == 'destroy') {
						myCharts.dispose();
					}
				}

				if (!myCharts) {// 不重复初始化
					var $option = $('textarea[name=option]', $this);
					var option;
					if ($option.size() > 0) {
						option = $.parseJSON($option.val());
					} else {
						option = {};
					}

					$.each($('textarea[name]', $this).not($option), function() {
						var $textarea = $(this);
						var name = $textarea.attr('name');
						var data;
						if ($textarea.size() > 0 && $textarea.val() != '') {
							data = $.parseJSON($textarea.val());
							option[name] = data;
						}
					});

					// 设置尺寸
					if ($this.height() < _height) {
						$this.css('height', _height);
					}
					$this.css('min-width', _min_width);

					// 为echarts对象加载数据
					myCharts = echarts.init($this.get());
					myCharts.setOption(option);
					_core._data.charts[id] = myCharts;
					var theme = $this.attr('theme');
					if (theme != undefined && theme != '') {
						myCharts.setTheme(theme);
					}
					// 绑定resize事件
					setTimeout(function() {
						window.onresize = function() {
							$.each(_core._data.charts, function(k, v) {
								v.resize();
							});
						}
					}, 200);
				}
			});
		};
	}

};
/**
 * 界面扩展初始化<br>
 * 只执行一次
 */
(function($) {
	_core.styleTable($);
	_core.styleBar($);
	_core.styleMsg($);
	_core.styleButton($);
	_core.styleTableBar($);
	_core.radioset($);
	_core.charts($);

	// 日期时间格式化函数
	Date.prototype.format = function(format) {
		/*
		 * format="yyyy-MM-dd hh:mm:ss";
		 */
		var o = {
			"M+" : this.getMonth() + 1,
			"d+" : this.getDate(),
			"h+" : this.getHours(),
			"m+" : this.getMinutes(),
			"s+" : this.getSeconds(),
			"q+" : Math.floor((this.getMonth() + 3) / 3),
			"S" : this.getMilliseconds()
		}

		if (/(y+)/.test(format)) {
			format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
		}

		for ( var k in o) {
			if (new RegExp("(" + k + ")").test(format)) {
				format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
			}
		}
		return format;
	};

})(jQuery);
