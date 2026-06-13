if (Core == undefined) {
	Core = {};
}

/**
 * 前置处理
 * 
 * @param {}
 *            zone
 */
Core.initBefore = function(zone) {
	// 初始化分割线
	$.each($(zone + 'table.ws-table[group=true]'), function() {
		var $table = $(this);

		var col = $table.attr('col');
		if (col == undefined) {
			col = 2;
		}
		if ($('tr:first', $table).attr('group') != 'true') {// 首行不是分割线则插入分割线
			$table.prepend('<tr group="true"><th>(未分组)</th></tr>');
		}
		var $div = $('<div accordion="true" multi="true"></div>');
		$table.after($div);
		$.each($('tr', $table), function() {
			var $tr = $(this);
			if ($tr.parents('table:first').attr('group') == 'true') {
				if ($tr.attr('group') == 'true') {// 分割线
					var show = $tr.attr('show');
					if (show == undefined) {
						show = 'true';// 默认显示
					}
					var title = $('th', $tr).html();
					var msg = $('td', $tr).size() > 0 ? $('td', $tr).html() : '';
					$div.append('<div title="' + title + '" msg="' + msg + '" show="' + show + '"></div>');
				} else if ($tr.attr('self') == 'true') {// 独立一个区域
					var $last = $('div[msg]:last>:last', $div);
					var $tmpDiv = $('<div style="margin-bottom: 5px;" class="ws-scroll"></div>');
					$tmpDiv.html($('td', $tr).html());
					if ($last.size() < 1) {
						$('div[msg]:last', $div).append($tmpDiv);
					} else {
						$last.after($tmpDiv);
					}
				} else {
					var $last = $('div[msg]:last>:last', $div);
					if ($last.size() < 1) {
						var $tmpTable = $('<table class="ws-table" col="' + col + '"></table>');
						$tmpTable.append($tr);
						$('div[msg]:last', $div).append($tmpTable);
					} else if ($last.attr("col") == undefined) {
						var $tmpTable = $('<table class="ws-table" col="' + col + '"></table>');
						$tmpTable.append($tr);
						$last.after($tmpTable);
					} else {
						$last.append($tr);
					}
				}
			}
		});
		$table.remove();
	});

	// 初始化多行table
	$.each($(zone + 'table.ws-table[col]'), function() {
		var $table = $(this);
		var col = $table.attr('col');
		$('tbody>tr:first', $table).siblings().attr('remove', 'true');// 添加删除标识
		$('tbody>tr:first', $table).attr('remove', 'true');
		var $tr = null;
		$.each($('tr[remove=true]', $table), function() {
			if ($tr != null && $tr.attr("total") >= col) {
				$table.append($tr);
				$tr = null;
			}

			var $currentTr = $(this);
			if ($currentTr.attr('whole') == 'true') {// 占据整行
				if ($tr != null) {// 补全
					var total = $tr.attr('total');
					if (total < col) {
						for (var i = 0; i < col - total; i++) {
							$tr.append('<th></th>').append('<td></td>');
						}
					}
					$table.append($tr);
				}
				$tr = $('<tr total="' + (parseInt(col) - 1) + '" class="whole"></tr>');
			} else if ($tr == null) {
				$tr = $('<tr total="0" class="row"></tr>');
			}

			// 到这里不为null了
			$tr.append($currentTr.html());
			$tr.attr('total', parseInt($tr.attr('total')) + 1);
		});
		if ($tr != null) {
			var total = $tr.attr('total');
			if (total < col) {
				for (var i = 0; i < col - total; i++) {
					$tr.append('<th></th>').append('<td></td>');
				}
			}
			$table.append($tr);
		}

		$('tr[remove=true]', $table).remove();

		// 设置比例
		var thWidth = parseInt(100 / col * 0.3);
		var tdWidth = parseInt(100 / col * 0.7);
		$('tr.row', $table).children('th').css('width', thWidth + '%').css("min-width", "100px");
		$('tr.row', $table).children('td').css('width', tdWidth + '%');
		$('tr.whole', $table).children('th').css('width', thWidth + '%').css("min-width", "100px");
		$('tr.whole', $table).children('td').attr("colspan", (col * 2 - 1)).css('width', (100 - thWidth) + '%');
	});
};

/**
 * 后置处理
 * 
 * @param {}
 *            zone
 */
Core.initAfter = function(zone) {
	// reset按钮
	$(zone + 'button:reset').click(function(event) {
		event.preventDefault();
		var $this = $(this);
		var $form = $this.parents('form:first');
		if ($form.attr('query') == 'true') {
			Core.resetQueryForm($form);
		} else {
			Widget.initAll($form);
		}
	});

	Core.applyEmptyTableState(zone);
};

/**
 * 为旧列表表格补空状态兜底。
 *
 * 仅匹配带 form 绑定且包含排序/勾选表头的 ws-table，避免影响普通表单布局表。
 */
Core.applyEmptyTableState = function(zone) {
	zone = zone || '';
	$.each($(zone + 'table.ws-table[form]'), function() {
		var $table = $(this);
		if ($('.bpmt-empty-table-row', $table).size() > 0) {
			return;
		}
		if ($('th[field], th[check]', $table).size() < 1) {
			return;
		}

		var hasDataRow = false;
		$('tr', $table).each(function() {
			var $tr = $(this);
			if ($tr.parents('table:first')[0] != $table[0]) {
				return;
			}
			if ($tr.hasClass('bpmt-empty-table-row') || $('th', $tr).size() > 0) {
				return;
			}
			if ($('td', $tr).size() > 0) {
				hasDataRow = true;
				return false;
			}
		});
		if (hasDataRow) {
			return;
		}

		var title = '没有匹配的数据';
		var body = '当前查询条件下没有数据。请调整筛选条件，或点击“重置查询”后重新查询。';
		if (typeof (_lan) != "undefined" && _lan == 'en') {
			title = 'No matching records';
			body = 'There are no records for the current query. Adjust filters, or reset the query and search again.';
		}
		var colCount = $('tr:first th', $table).size();
		if (colCount < 1) {
			colCount = 99;
		}
		var $row = $('<tr class="bpmt-empty-table-row"><td colspan="' + colCount
				+ '"><div class="bpmt-state bpmt-state-empty"><strong>' + title
				+ '</strong><span>' + body + '</span></div></td></tr>');
		var $tbody = $('tbody:first', $table);
		if ($tbody.size() > 0) {
			$tbody.append($row);
			return;
		}
		var $footer = $('tr:has(th.ws-bar):first', $table);
		if ($footer.size() > 0) {
			$footer.before($row);
		} else {
			$table.append($row);
		}
	});
};

/**
 * 清空查询表单并重新查询。
 *
 * 浏览器原生 reset 会回到服务端渲染时的当前 query 值；在筛选无结果页点击
 * “重置查询”时，这会让用户停留在同一组筛选结果中。
 */
Core.resetQueryForm = function($form) {
	$('input', $form).each(function() {
		var $input = $(this);
		var name = $input.attr('name') || '';
		var type = ($input.attr('type') || 'text').toLowerCase();

		if (type == 'hidden') {
			if (name == '_page') {
				$input.val('1');
			} else if (name == '_field' || name == '_dir') {
				$input.val('');
			}
			return;
		}

		if (type == 'checkbox') {
			$input.prop('checked', false);
			return;
		}

		if (type == 'radio' || type == 'button' || type == 'submit' || type == 'reset') {
			return;
		}

		$input.val('');
	});

	$('textarea', $form).each(function() {
		var $textarea = $(this);
		var name = $textarea.attr('name') || '';
		if (name.indexOf('_') == 0) {
			return;
		}
		$textarea.val('');
	});

	$('select', $form).each(function() {
		$(this).val('');
	});

	$('.chzn-container', $form).prev('select').trigger('liszt:updated').trigger('chosen:updated');
	$form.submit();
};
