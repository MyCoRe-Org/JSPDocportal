<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<mcr:session var="lang" info="language" />

  <div id="latest_documents">
    <template v-if="solrResponse">
      <div class="ir-latestdocs card"style="background-color:unset">
        <div class="card-header px-0 py-2" style="background-color:unset">
          <h4 class="mb-0"><a href="${WebApplicationBaseURL}do/browse/${it.path}"><fmt:message key="Browse.Search.LatestDocs" /></a></h4>
        </div>
        <div>
          <template v-for="doc in solrResponse.docs">
            <div class="card ir-latestdocs-card">
              <div class="card-body">
                <template v-if="doc['ir.creator.result']">
                  <p class="card-text" v-text="doc['ir.creator.result']"></p>
                </template>
                <template v-if="doc['ir.title.result']">
                  <h5 class="card-title" v-for="title in [ doc['ir.title.result'] ]">
                    <a class="card-link" href="#"
                       v-bind:href="baseurl + 'resolve/id/'+doc['id']" 
                       v-text="title.length > 120 ? title.substring(0,100) + '…' : title"></a>
                  </h5>
                </template>
                <table v-for="hascover in [ doc['id'] && doc['id'].indexOf('_bundle') == -1 && doc['ir.doctype.result']!='Datenpublikation' ]">
                  <col v-bind:width="hascover ? '67%' : '100%'">
                  <col v-bind:width="hascover ? '33%' : '0%'">
                  <tr>
                    <td style="vertical-align: top">
                      <p class="card-text" v-show="doc['ir.originInfo.result']" 
                         v-text="doc['ir.originInfo.result']"></p>
                      <p class="card-text text-secondary fw-bold" v-show="doc['ir.doctype.result']"
                         v-text="'${lang}'=='en' && doc['ir.doctype_en.result'] ? doc['ir.doctype_en.result'] :  doc['ir.doctype.result']" ></p>
                      <p class="card-text text-secondary" 
                         v-text="doc['created'].substring(8,10)+'.'+doc['created'].substring(5,7)+'.'+doc['created'].substring(0,4)" ></p>
                    </td>
                    <td style="vertical-align: bottom; width: 33%; padding-left: 15px; text-align:right">
                      <template v-if="hascover">
                        <a v-bind:href="'../resolve/id/' + doc['id']">
                          <img class="ir-latestdocs-cover" style="max-width: 100%; max-height: 180px; object-fit: contain;" 
                               v-bind:src="baseurl + 'api/iiif/image/v2/thumbnail/' + doc['id'] + '/full/!1024,1024/0/default.jpg'"
                               onerror="this.onerror=null; this.src='${WebApplicationBaseURL}images/empty_pixel.png'" />
                        </a>
                      </template>
                    </td>
                  </tr>
                </table>
              </div>
            </div> 
          </template>
        </div>
        <div class="pb-3">
          <a href="${WebApplicationBaseURL}do/browse/${it.path}" 
             class="ir-latestdocs-more-button btn btn-sm btn-primary float-end mt-3">
             <fmt:message key="Browse.Search.LatestDocs.button.more" />
          </a>
        </div>      
      </div>
    </template>
  </div>
