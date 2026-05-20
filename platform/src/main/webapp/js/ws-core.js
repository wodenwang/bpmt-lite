/**
 * 核心js类
 *
 * @author wodenwang
 */
var Core = {

	/*
	 * private
	 */
	/**
	 * 自动编号，用于生成自动ID。
	 */
	seq : 1,

	/**
	 * 函数集
	 */
	fn : {},

	/*
	 * public方法
	 */

	/**
	 * 设置/获取函数
	 */
	fn : function(zone, name, fn) {
		if (fn == undefined || !$.isFunction(fn)) {
			return Core.getFn(zone, name);
		} else {
			Core.setFn(zone, name, fn);
		}
	},

	/**
	 * 设置函数
	 */
	setFn : function(zone, name, fn) {
		if (typeof (zone) != 'string') {
			var id = zone.attr('id');
			if (id == undefined || id == null) {
				id = Core.nextSeq();
				zone.attr('id', id);
			}
			zone = id;
		}
		var _fn = Core.fn[zone];
		if (_fn == undefined) {
			_fn = {};
			Core.fn[zone] = _fn;
		}

		_fn[name] = fn;
	},

	/**
	 * 获取函数
	 */
	getFn : function(zone, name) {
		if (typeof (zone) != 'string') {
			zone = zone.attr('id');
		}
		var _fn = Core.fn[zone];
		if (_fn == undefined) {
			_fn = {};
			Core.fn[zone] = _fn;
		}
		return _fn[name];
	},

	/**
	 * 清理整页加载和浏览器恢复时遗留的遮罩。
	 */
	clearPageMask : function() {
		if (typeof ($) == "undefined") {
			return;
		}

		$('#loading').hide();

		var $openDialogs = $('.ui-dialog:visible').filter(function() {
			return $('.ui-dialog-content:visible', this).size() > 0;
		});
		if ($openDialogs.size() < 1) {
			$('.ui-widget-overlay').remove();
			$('body').removeClass('ui-dialog-open');
		}

		var $openAmModals = $('.am-modal:visible').not('#_body_loading');
		if ($openAmModals.size() < 1) {
			$('#_body_loading').hide().removeClass('am-modal-active am-active');
			$('.am-dimmer').removeClass('am-active').hide();
		}
	},

	/**
	 * 界面（区域）初始化
	 *
	 * @param zone
	 *            区域id
	 */
	init : function(zone) {
		var zoneId;

		if (!zone || zone == '' || zone == '_body') {
			// 硬塞一个div
			Core.clearPageMask();
			var $div = $('<div id="_body"></div>');
			$div.append($('body').children());
			$('body').append($div);
			zone = '#_body ';
			zoneId = '_body';
			setTimeout(function() {
				Core.clearPageMask();
			}, 0);
		} else {
			zoneId = zone;
			zone = '#' + zone + ' ';
		}

		// 前置初始化
		if ($.isFunction(Core.initBefore)) {
			Core.initBefore(zone);
		}

		$(zone + 'table.ws-table').styleTable();
		$(zone + "button").styleButton();
		$(zone + "div.ws-bar").styleBar();
		$(zone + "div.ws-msg.info").styleMsg({
			type : 'info'
		});
		$(zone + "div.ws-msg.warning").styleMsg({
			type : 'warning'
		});
		$(zone + "div.ws-msg.error").styleMsg({
			type : 'error'
		});
		$(zone + "div.ws-msg.normal").styleMsg({
			type : 'normal'
		});
		$(zone + "th.ws-bar").styleTableBar();

		// 初始化panel
		$.each($(zone + 'div[panel]'), function() {
			var $this = $(this);
			var expand = $this.attr("expand");// 是否收缩
			var tittle = $this.attr("panel");
			var height = $this.attr('height');
			var $h3 = $('<h3 class="ui-widget-header ui-corner-all"></h3>');
			$h3.html(tittle);
			var $div = $('<div></div>');
			$this.addClass('ui-widget-content').addClass('ui-corner-all').addClass('ws-panel');
			$div.html($this.html());
			$this.html('');
			$this.append($h3);
			$this.append($div);
			// 是否收起
			if ("true" == expand) {
				$h3.append('<span class="ui-icon ui-icon-minusthick" style="float: right;cursor:pointer;"></span>');
				$(".ui-icon", $h3).click(function() {
					$(this).toggleClass("ui-icon-minusthick").toggleClass("ui-icon-plusthick");
					$div.toggle();
					$("div.ws-charts", $this).charts('resize');
				});
			}
			if (height != undefined && $.isNumeric(height) && 0 + height > 0) {
				if (0 + height < 200) {// 最小高度200
					height = 200;
				}
				$div.css('height', '' + height + 'px');
				$div.css('min-height', '' + height + 'px');
				$div.css('overflow', 'auto');
			}
		});

		// 初始化tab
		$.each($(zone + 'div[tabs=true]'), function() {
			var $this = $(this);
			$this.addClass('ws-tabs');
			var $ul = $("<ul></ul>");
			var active = $this.attr('active');
			if (active == undefined || isNaN(active)) {
				active = 0;
			}
			$.each($this.children('div'), function(index) {
				var $div = $(this);
				var id = $div.attr('id');
				var title = $div.attr('title');
				var style = $div.attr("tabStyle");
				$div.removeAttr('title');
				if (id == null || id == '') {
					id = zoneId + '_' + Core.nextSeq();
					$(this).attr('id', id);
				}
				if (title == null || title == '') {
					title = '标签' + id;
				}

				var $li = $('<li><a href="#' + id + '">' + title + '</a></li>');
				if (style != undefined && style != '') {
					$('a', $li).attr('style', style);
				}

				$div.attr("tab", index);
				$('a', $li).attr("tab", index);
				var close = $div.attr("close");
				if (close == 'true') {
					var $span = $('<span class="ui-icon ui-icon-close" role="presentation" style=" float: right; margin: 0.4em 0.2em 0 0; cursor: pointer;">关闭</span>');
					$span.click(function() {
						var panelId = $(this).closest("li").remove().attr("aria-controls");
						$("#" + panelId).remove();
						$this.tabs("refresh");
					});
					$li.append($span);
				}
				$ul.append($li);
			});

			$this.prepend($ul);
			var tabs = $this.tabs({
				activate : function(event, ui) {
					$('textarea', ui.newPanel).blur();// 解决codemirror无法渲染的问题
					$("div.ws-charts", ui.newPanel).charts('resize');
				},
				active : active
			});

			// 方向
			var btn = $this.attr("button");
			if (btn && btn == 'left') {
				tabs.addClass("ui-tabs-vertical ui-helper-clearfix");
				$('li', $this).removeClass("ui-corner-top").addClass("ui-corner-left");
			}

			// 排序
			var sort = $this.attr("sort");
			if (sort && sort != '') {
				$('li', $this).css("cursor", "move");
				tabs.find(".ui-tabs-nav").sortable({
					axis : sort,
					stop : function(event, ui) {
						ui.item.click();
						// div也排序
						var divId = $('a', ui.item).attr("href");
						var $div = $(divId);
						var $prevLi = ui.item.prev();
						var prevId = $('a', $prevLi).size() > 0 ? $('a', $prevLi).attr("href") : null;

						if (prevId != null) {
							$(prevId).after($div);
						} else {// 到头了
							$div.parent().children('ul:first').after($div);
						}

					}
				});
			}
		});

		// accordion初始化
		$.each($(zone + 'div[accordion=true]'), function() {
			var $this = $(this);
			$this.addClass('ws-tabs');
			var multi = $this.attr('multi');

			$.each($this.children('div'), function(index) {
				var $div = $(this);
				var id = $div.attr('id');
				var title = $div.attr('title');
				var msg = $div.attr('msg');
				$div.removeAttr('title');
				if (id == null || id == '') {
					id = zoneId + '_' + Core.nextSeq();
					$(this).attr('id', id);
				}
				if (title == undefined || title == '') {
					title = '标签' + id;
				}
				if (msg != undefined && msg != '') {
					title += '<span style="color:red;cursor:help;" tip="true" title="' + msg + '">(提示)</span>';
				}
				var $h3 = $('<h3><a href="#">' + title + '</a></h3>');
				$div.before($h3);
			});

			if (multi == 'true') {
				$this.accordion({
					heightStyle : "content",
					collapsible : true,
					beforeActivate : function(event, ui) {
						// The accordion believes a panel is being opened
						if (ui.newHeader[0]) {
							var currHeader = ui.newHeader;
							var currContent = currHeader.next('.ui-accordion-content');
							// The accordion believes a panel is being closed
						} else {
							var currHeader = ui.oldHeader;
							var currContent = currHeader.next('.ui-accordion-content');
						}
						// Since we've changed the default behavior, this
						// detects the actual status
						var isPanelSelected = currHeader.attr('aria-selected') == 'true';

						// Toggle the panel's header
						currHeader.toggleClass('ui-corner-all', isPanelSelected).toggleClass('accordion-header-active ui-state-active ui-corner-top', !isPanelSelected).attr('aria-selected',
								((!isPanelSelected).toString()));

						// Toggle the panel's icon
						currHeader.children('.ui-icon').toggleClass('ui-icon-triangle-1-e', isPanelSelected).toggleClass('ui-icon-triangle-1-s', !isPanelSelected);

						// Toggle the panel's content
						currContent.toggleClass('accordion-content-active', !isPanelSelected)
						if (isPanelSelected) {
							currContent.slideUp(100);
						} else {
							currContent.slideDown(100);
						}

						$('textarea', ui.newPanel).blur();// 解决codemirror无法渲染的问题
						$("div.ws-charts", ui.newPanel).charts('resize');

						return false; // Cancels the default action
					}
				});

				$.each($this.children('.ui-accordion-content'), function(index) {
					var show = $(this).attr('show');
					var $content = $(this);
					var $header = $content.prev('.ui-accordion-header:first');
					if (show != undefined && show == 'false') {
						$content.toggleClass('accordion-content-active', false).slideUp(0);
						$header.toggleClass('ui-corner-all', true).toggleClass('accordion-header-active ui-state-active ui-corner-top', false).attr('aria-selected', null);
						$header.children('.ui-icon').toggleClass('ui-icon-triangle-1-e', true).toggleClass('ui-icon-triangle-1-s', false);
					} else {
						$content.toggleClass('accordion-content-active', true).slideDown(0);
						$header.toggleClass('ui-corner-all', false).toggleClass('accordion-header-active ui-state-active ui-corner-top', true).attr('aria-selected', 'true');
						$header.children('.ui-icon').toggleClass('ui-icon-triangle-1-e', false).toggleClass('ui-icon-triangle-1-s', true);
					}
				});

			} else {
				$this.accordion({
					heightStyle : "content",
					collapsible : true
				});
			}
		});

		// 初始化div init标签
		$.each($(zone + 'div[init]'), function() {
			var $this = $(this);
			$this.addClass('ws-zone');
			var id = $(this).attr('id');
			if (id == null || id == '') {
				id = zoneId + '_' + Core.nextSeq();
				$this.attr('id', id);
			}
			var url = $this.attr('init');
			var tabindex = $this.attr("tab");
			if (tabindex != null && 0 + tabindex > 0) {// 非活动标签则延迟加载
				$('ul li a[tab=' + tabindex + ']:first', $this.parent()).click(function() {
					var $a = $(this);
					var tabloaded = $this.attr("tabloaded");
					if (tabloaded == 'true') {
						return;
					}

					// disabled的返回true
					if ($a.parent('li.ui-state-disabled').size() > 0) {// disabled
						return;
					}

					// 获取最新的url
					url = $this.attr("init");
					if (url != null && url != '') {
						Ajax.post(id, url);
					}
					$this.attr("tabloaded", "true");
				});
			} else {
				if (url != null && url != '') {
					Ajax.post(id, url);
				}
			}
		});

		// 初始化table分页
		Core.initListTable(zone);

		// tooltip泡泡
		$.each($(zone + "[tip=true]"), function() {
			var $this = $(this);
			var title = $this.attr('title');
			var selector = $this.attr('selector');
			// 优先处理selector
			if (selector != undefined && selector != '') {
				$this.attr('title', '-');
				var $div = $('<div></div>');
				$div.append($(selector, $this).clone());
				$(selector, $this).remove();
				$this.tooltip({
					track : true,
					content : $div.html()
				});
			} else {
				$this.tooltip({
					track : true,
					content : $('<div>' + title + '</div>').styleMsg({
						type : 'info'
					}).html()
				});
			}
			$this.addClass('ws-tip');
		});

		// 图标
		$.each($(zone + 'span[icon]'), function() {
			var icon = $(this).attr("icon");
			$(this).addClass("ui-icon");
			$(this).addClass("ui-icon-" + icon);
			$(this).attr("style", "display:inline-block");
		});

		// 下拉框
		$.each($(zone + 'select.chosen'), function() {
			var $select = $(this);
			var noResultsText = "找不到结果";
			var selectText = "请选择";
			if (typeof (_lan) != "undefined" && _lan == 'en') {
				noResultsText = "No Result";
				selectText = "Select";
			}
			$select.show();
			$select.chosen({
				no_results_text : noResultsText,
				placeholder_text : selectText
			});
		});

		// 日期时间组件
		$.each($(zone + 'input.date'), function() {
			var defaultDate = $(this).attr('defaultDate');
			if (defaultDate == undefined || defaultDate == '') {
				defaultDate = null;
			}
			$(this).datepicker({
				defaultDate : defaultDate,
				showButtonPanel : true,
				changeMonth : true,
				changeYear : true,
				dateFormat : 'yy-mm-dd'
			});
		});
		$.each($(zone + 'input.time'), function() {
			var defaultDate = $(this).attr('defaultDate');
			if (defaultDate == undefined || defaultDate == '') {
				defaultDate = null;
			}
			$(this).timepicker({
				defaultDate : defaultDate,
				showSecond : true,
				closeText : '确定',
				timeFormat : 'HH:mm:ss'
			});
		});
		$.each($(zone + 'input.datetime'), function() {
			var defaultDate = $(this).attr('defaultDate');
			if (defaultDate == undefined || defaultDate == '') {
				defaultDate = null;
			}
			$(this).datetimepicker({
				defaultDate : defaultDate,
				showButtonPanel : true,
				showSecond : true,
				changeMonth : true,
				changeYear : true,
				closeText : '确定',
				dateFormat : 'yy-mm-dd',
				timeFormat : 'HH:mm:ss'
			});
		});
		$.each($(zone + 'input.yearmonth'), function() {
			var defaultDate = $(this).attr('defaultDate');
			if (defaultDate == undefined || defaultDate == '') {
				defaultDate = null;
			}
			$(this).datepicker({
				defaultDate : defaultDate,
				showButtonPanel : true,
				changeMonth : true,
				changeYear : true,
				currentText : '本月',
				closeText : '确定',
				dateFormat : 'yymm',
				onClose : function(dateText, inst) {
					var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
					var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
					$(this).datepicker('setDate', new Date(year, month, 1));
				},
				beforeShow : function(input, inst) {
					var datestr;
					if ((datestr = $(this).val()).length > 0) {
						year = datestr.substring(0, 4);
						month = datestr.substring(4, 6);
						$(this).datepicker('option', 'defaultDate', new Date(year, new Number(month) - 1, 1));
						$(this).datepicker('setDate', new Date(year, new Number(month) - 1, 1));
					}
				},
				beforeShowDay : function(d) {
					return [ false, null, '请选择上方月份,无需选择日期.点击右下方确定.' ];
				}
			});
		});

		// 颜色选择组件
		$.each($(zone + 'input.colorpicker'), function() {
			$(this).colorPicker();
		});

		// 处理数字控件
		$.each($(zone + 'input[class],' + zone + 'select[class],' + zone + 'textarea[class]'), function() {
			if ($(this).attr("readonly") == "readonly" || $(this).attr("disabled") == "disabled") {
				return;
			}
			try {
				var data = $(this).metadata();
				// 处理必填
				if (data.required) {
					$(this).parent().append('<span style="color:red;font-width:bold;"> * </span>');
				}

				if ($(this).attr('type') == 'text') {
					var max = data.max != undefined ? data.max : null;
					var min = data.min != undefined ? data.min : null;
					if (data.number) {// 复数
						$(this).spinner({
							numberFormat : "n",
							min : min,
							max : max,
							change : function(event, ui) {
								var val = $(this).val().replace(/,/, "");
								$(this).val(val);
							}
						});
					} else if (data.digits) {// 正整数
						if (min == null) {
							min = 0;
						}
						$(this).spinner({
							min : min,
							max : max,
							change : function(event, ui) {
								var val = $(this).val().replace(/,/, "");
								$(this).val(val);
							}
						});
					} else if (data.integer) {// 正负整数
						$(this).spinner({
							min : min,
							max : max,
							change : function(event, ui) {
								var val = $(this).val().replace(/,/, "");
								$(this).val(val);
							}
						});
					}
				}
			} catch (e) {
				// nothing
			}
		});

		// 初始化表单
		$.each($(zone + 'form'), function() {
			var $this = $(this);
			var target = $this.attr('zone');
			var sync = $this.attr('sync');
			var option = $this.attr('option');

			// 清除表单MAP
			if (Widget != undefined && $.isFunction(Widget.clean)) {
				Widget.clean($this);
			}

			if ('true' == sync) {
				// 同步表单则不做ajax处理
				return;
			}
			var $this = $(this);
			if (!target || target == null) {
				target = zoneId;
			}

			if (option == null) {
				option = {};
			} else {
				option = eval('(' + option + ')');
			}

			// 綁定事件
			$this.on('submit', function(event) {
				event.preventDefault();
				Ajax.form(target, $this, option);
			});
		});

		// 处理radio
		$(zone + 'input:radio,' + zone + 'input:checkbox').radioset();

		// 按钮組
		$(zone + ".ws-group").buttonset();

		// 代码编辑器
		$.each($(zone + 'textarea[code=true]'), function() {
			var $this = $(this);
			var width = $this.attr('width');
			if (width == undefined || width == null || width == '') {
				width = "";
			} else {
				width = width + "px";
			}
			var height = $this.attr('height');
			if (height == undefined || height == null || height == '') {
				height = "";
			} else {
				height = height + "px";
			}

			var setFullScreen = function(cm, full) {
				var wrap = cm.getWrapperElement();
				if (full) {
					wrap.className += " CodeMirror-fullscreen";
					wrap.style.height = "";
					wrap.style.width = "";
					document.documentElement.style.overflow = "hidden";
				} else {
					wrap.className = wrap.className.replace(" CodeMirror-fullscreen", "");
					wrap.style.height = height;
					wrap.style.width = width;
					document.documentElement.style.overflow = "";
				}
				cm.refresh();
			};
			var defCodemirrorOption = {
				lineNumbers : true,
				styleActiveLine : true,
				matchBrackets : true,
				theme : "eclipse",
				extraKeys : {
					"F10" : function(cm) {
						var isFullScreen = /\bCodeMirror-fullscreen\b/.test(cm.getWrapperElement().className);
						setFullScreen(cm, !isFullScreen);
					},
					"Esc" : function(cm) {
						setFullScreen(cm, false);
					},
					"Ctrl-F10" : function(cm) {
						var l = [ 'groovy', 'javascript', 'sql', 'html', 'json' ];
						for (var i = 0; i < l.length; i++) {
							if (l[i] == $this.attr('mode')) {
								var j;
								if (i == l.length - 1) {
									j = 0;
								} else {
									j = i + 1;
								}
								setMode(l[j]);
								$this.attr('mode', l[j]);
								if (console) {
									console.log("codemirror代码编辑器 - 切换语言:" + l[j]);
								}
								break;
							}
						}
					}
				}
			};

			var option = eval('(' + $this.attr("option") + ')');
			option = $.extend({}, defCodemirrorOption, option);
			var editor = CodeMirror.fromTextArea(this, option);

			var setMode = function(mode) {
				CodeMirror.modeURL = _cp + "/js/codemirror/mode/%N/%N.js";
				if (mode && mode != '') {
					var info = CodeMirror.findModeByName(mode);
					if (info) {
						editor.setOption("mode", info.mime);
						CodeMirror.autoLoadMode(editor, info.mode);
					}
				}
			}
			setMode($this.attr('mode'));// 调用一次

			{
				var wrap = editor.getWrapperElement();
				wrap.style.height = height;
				wrap.style.width = width;
				editor.refresh();
			}

			editor.on("change", function(instance, changeObj) {
				instance.save();
			});
			$this.blur(function() {
				editor.setValue($(this).val());
			});
		});

		// 初始化图表
		$(zone + "div.ws-charts:visible").charts();

		// portal拖动
		$.each($(zone + '.ws-column'), function() {
			var $div = $(this);
			var scale = $div.attr("scale");
			if (scale == null) {
				scale = 'auto';
			} else {
				scale = scale + "%";
			}
			$div.css("width", scale);

			var sortable = $div.attr("sortable");
			if ('true' == sortable) {
				$('div>h3', $div).css("cursor", "move");
			}
		});
		$(zone + ".ws-column[sortable='true']").sortable({
			connectWith : ".ws-column"
		});

		// 后置初始化
		if ($.isFunction(Core.initAfter)) {
			Core.initAfter(zone);
		}

	},

	/**
	 * 初始化分页,查询等
	 */
	initListTable : function(zone) {

		// 创建隐藏form
		var newForm = function(formid, paramsid) {
			var form;
			if (formid == undefined || formid == null || formid == '' || $('#' + formid).size() < 1) {
				formid = $(zone).attr('id') + '_' + (Core.nextSeq());
				if ($('#' + formid).size() < 1) {
					form = $('<form method="post"></form>');
					form.attr("id", formid);
					form.attr("action", $(zone).attr("loaded"));
					$(zone).before(form);
					form.on('submit', function(event) {
						event.preventDefault();
						Ajax.form($(zone).attr("id"), form);
					});
				}
			}
			form = $('#' + formid);

			// 插入隐藏field
			if ($('input[name=_field]', form).size() < 1) {
				form.append('<input name="_field" type="hidden"/>');
			}
			// 插入隐藏dir
			if ($('input[name=_dir]', form).size() < 1) {
				form.append('<input name="_dir" type="hidden"/>');
			}
			// 插入隐藏page
			if ($('input[name=_page]', form).size() < 1) {
				form.append('<input name="_page" type="hidden"/>');
			}
			// 插入隐藏limit
			if ($('input[name=_limit]', form).size() < 1) {
				form.append('<input name="_limit" type="hidden"/>');
			}

			// 插入隐藏的params
			if ($('[name=_params]', form).size() < 1 && (paramsid != undefined || paramsid == null || paramsid == '')) {
				var $params = $('#' + paramsid);
				if ($params.size() > 0) {
					form.append('<textarea name="_params" style="display:none;">' + $params.val() + '</textarea>');
				}
			}

			return form;
		};

		// 查询框
		$.each($(zone + 'form[query=true]'), function() {
			var $this = $(this);
			// 点击查询按钮则认为是全新一次查询,分页标识初始化
			$this.find(":submit").click(function() {
				if ($('input[name=_page]', $this).size() > 0) {
					$('input[name=_page]', $this).val('1');
				}
			});
		});

		// 处理排序
		$.each($(zone + 'th[field]'), function() {
			var $this = $(this);
			var field = $this.attr('field');
			if (field == null || field == '') {
				return;
			}
			var table = $this.parents('table').first();
			var formid = table.attr("form");
			var paramsid = table.attr("params");
			var form = newForm(formid, paramsid);
			table.attr("form", form.attr('id'));

			var fieldVal = $('input[name=_field]', $('#' + formid)).val();
			var dirVal = $('input[name=_dir]', $('#' + formid)).val();
			if (field == fieldVal) {
				if (dirVal == 'asc') {
					$this.append('<span icon="arrowthick-1-n"></span>');
				} else if (dirVal == 'desc') {
					$this.append('<span icon="arrowthick-1-s"></span>');
				}
			}

			// 绑定事件
			$this.click(function(event) {
				var $field = $('input[name=_field]', form);
				var $dir = $('input[name=_dir]', form);
				if ($field.val() != field || $dir.val() == '') {
					$dir.val('desc');
					$field.val(field);
				} else if ($dir.val() == 'desc') {
					$dir.val('asc');
					$field.val(field);
				} else {
					$dir.val('');
					$field.val('');
				}

				$('input[name=_page]', form).val('1');// 页码强制为1.
				var target = form.attr('zone');
				if (target == null || target == '') {
					target = $(zone).attr('id');
				}
				Ajax.form(target, form);
			});
			$this.css('cursor', 'pointer');

		});

		// 处理分页
		$.each($(zone + 'div.ws-bar.page'), function() {
			// 处理文字区域，左侧纵向居中
			var $this = $(this);
			var $table = $this.prev('table.ws-table').first();
			if ($table.size() <= 0) {
				$table = $this.prev('div.ws-scroll:first').find('table.ws-table:first');// 加入ws-scroll后的处理
			}

			var formid = $this.attr('form');
			if (formid == undefined || formid == null || formid == '') {// 没有指定绑定form
				if ($table.size() > 0) {
					formid = $table.attr("form");
				}
			}
			var paramsid = $this.attr('params');
			if (paramsid == undefined || paramsid == null || paramsid == '') {// 没有指定绑定form
				if ($table.size() > 0) {
					paramsid = $table.attr("params");
				}
			}
			var form = newForm(formid, paramsid);
			$this.attr("form", form.attr('id'));

			// 默认分页
			var limit = $this.attr("limit");
			if (limit != undefined && limit != null && limit != '' && new Number(limit) > 0) {
				var $limit = $('input[name=_limit]', form);
				if ($limit.val() == null || $limit.val() == '') {
					$limit.val(limit);
				}
			}

			$this.find("button").click(function() {
				var val = $(this).val();
				var $page = $('input[name=_page]', form);
				$page.val(val);
				var target = form.attr('zone');
				if (target == null || target == '') {
					target = $(zone).attr('id');
				}
				Ajax.form(target, form);
			});
		});

		// 处理选择框
		$.each($(zone + 'th[check=true]'), function() {
			var $th = $(this);
			var $table = $th.parents('table:first');
			// 处理td
			$.each($('td[check=true]', $table), function() {
				var $td = $(this);
				var value = $td.attr("value");
				var name = $td.attr("checkname");
				if (name == undefined || name == null || name == '') {
					name = "_keys";
				}
				var checkstate = $td.attr("checkstate");
				var $checkbox = $('<input name="' + name + '" type="checkbox" value="' + value + '"/>');
				$checkbox.on('change', function() {
					if ($(this).prop("checked")) {
						$td.parent('tr').addClass("ui-state-focus");
						$('td', $td.parent('tr')).addClass("ui-state-focus");
					} else {
						$td.parent('tr').removeClass("ui-state-focus");
						$('td', $td.parent('tr')).removeClass("ui-state-focus");
					}
				});
				$td.append($checkbox);
				$td.addClass("ws-checkbox");
				if (checkstate != undefined && checkstate == 'true') {
					$checkbox.click();
				}

			});

			// 处理th的checkbox
			var $thCheckbox = $('<input type="checkbox" />');
			$thCheckbox.click(function() {
				if ($(this).prop("checked")) {
					$('td[check=true] input:unchecked', $table).click();
				} else {
					$('td[check=true] input:checked', $table).click();
				}
			});
			$th.append($thCheckbox);
			$th.addClass("ws-checkbox");
		});

	},

	/**
	 * 获取当前页自动递增号
	 * 
	 * @returns
	 */
	nextSeq : function() {
		return Core.seq++;
	}
};

if (typeof ($) != "undefined") {
	$(window).bind('load pageshow', function() {
		setTimeout(function() {
			Core.clearPageMask();
		}, 0);
	});
}
