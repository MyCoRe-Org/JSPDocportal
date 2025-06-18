<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<fmt:message var="pageTitle" key="Webpage.feedback" />

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="fragments/html_head.jspf" %>
</head>
<body>
  <%@ include file="fragments/header.jspf" %>
  <div id="content_area">
  <div class="container">
  <div class="row">
  <div class="col">
		<div class="ir-box">
			<mcr:includeWebcontent id="feedback" file="feedback.html" />

          <c:forEach var="m" items="${it.messages}">
            <div class="alert alert-info" role="alert">
              <c:out value="${m}" />
            </div>
          </c:forEach>

			<form id="feedback-form" action="${applicationScope.WebApplicationBaseURL}do/feedback" accept-charset="UTF-8" method="post"
				class="form-horizontal ir-box">
				<input name="csrfToken" type="hidden" id="csrf-token" value="" />
				<input type="hidden" name="returnURL" value="${it.returnURL}" />
				<input type="hidden" name="subject" value="${it.subject}" />
				<input type="hidden" name="recipient" value="${it.recipient}" />
				<input type="hidden" name="topicHeader" value="${it.topicHeader}" />
				<input type="hidden" name="topicURL" value="${it.topicURL}" />

				<div class="row">
					<label class="col-sm-2 control-label"><fmt:message
							key="Webpage.feedback.label.recipient" /></label>
					<div class="col-sm-10">
						<p class="form-control-static">
							<b>${it.recipient}</b>
						</p>
					</div>
				</div>
				<div class="row">
					<label class="col-sm-2 control-label"><fmt:message
							key="Webpage.feedback.label.topic" /></label>
					<div class="col-sm-10">
						<h4 class="form-control-static" style="margin-top: 0px">${it.topicHeader}<br />(${it.topicURL})
						</h4>
					</div>
				</div>

				<div class="row">
					<label for="inputName" class="col-sm-2 control-label"><fmt:message
							key="Webpage.feedback.label.senderName" /></label>
					<div class="col-sm-10">
						<input type="text" class="form-control" id="inputName" name="fromName" />
					</div>
				</div>
				<div class="row">
					<label for="inputEmail" class="col-sm-2 control-label"><fmt:message
							key="Webpage.feedback.label.senderEmail" /></label>
					<div class="col-sm-10">
						<input type="text" class="form-control" id="inputEmail" name="fromEmail" />
					</div>
				</div>
				<div class="row">
					<label for="inputEmail" class="col-sm-2 control-label"><fmt:message
							key="Webpage.feedback.label.message" /></label>
					<div class="col-sm-10">
						<textarea class="form-control" id="message" rows="10" name="message"></textarea> 
					</div>
				</div>
				<hr />
				<div class="row">
					<label class="col-sm-2 control-label"></label>
					<div class="col-sm-10">
						<fmt:message key="Webpage.feedback.button.send" var="lblSend" />
						<input name="doSend" type="submit" id="submit-button" style="display:none" disabled="disabled" value="${lblSend}"  />
					    <button type="button" class="btn btn-primary" onclick="submitForm('${it.csrfToken}')">${lblSend}</button>
					</div>
				</div>
			</form>
			<script type="text/javascript">
				function submitForm(csrf){
					document.getElementById('csrf-token').value = csrf;
					document.getElementById('submit-button').disabled=false;
					document.getElementById('submit-button').click();
				}
			</script>
		 </div>
     </div>
     </div>
     </div>
     </div>
  <%@ include file="fragments/footer.jspf" %>
  </body>
</html>
