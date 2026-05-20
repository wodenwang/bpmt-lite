<%@ page language="java" pageEncoding="UTF-8"%>
<c:if test="${(_head==null && param._head != 'false') || (_head!=false && param._head!='false')}">
		<c:if test="${_zone == null || _zone == '' || _zone == '_body'}">
			<script type="text/javascript">
				if (window.Core && typeof Core.clearPageMask == 'function') {
					Core.clearPageMask();
				} else {
					document.write('<style>#loading{display:none}<\/style>');
				}
			</script>
			</body>
			</html>
	</c:if>
</c:if>
