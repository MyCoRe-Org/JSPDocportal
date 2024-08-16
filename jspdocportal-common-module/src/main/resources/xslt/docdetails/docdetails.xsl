<?xml version="1.0"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                
                xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
                exclude-result-prefixes="xsl fn xs mcri18n"
                expand-text="yes">
  <xsl:output method="html" indent="yes" standalone="yes" encoding="UTF-8"/>
  
  <!-- ensure that the i18n functions are imported before docdetails.xsl in your stylesheet:
  <xsl:import href="resource:xslt/functions/i18n.xsl" />
   -->

  <xsl:template name="dd_block">
    <xsl:param name="key" as="xs:string" />
    <xsl:param name="showInfo" as="xs:boolean" select="true()" />
    <xsl:param name="labelkey" as="xs:string" select="''"/>
    <xsl:param name="css_class" as="xs:string" select="'col1'"/>
    <xsl:param name="items" as="node()" />
    
    <div id="docdetails-{$key}"  class="docdetails-block">
       <!-- printInfoLabel(ctx, out); -->
       <span class="docdetails-info">
         <xsl:choose>
          <xsl:when test="$showInfo">
            <xsl:variable name="info" select="mcri18n:translate(concat($labelkey, '.info'))" />
              <a id="infoButton_{$key}" class="float-left docdetails-info-btn" data-toggle="popover">
                <i class="fa fa-info-circle"></i>
              </a>
              <script>
                $(document).ready(function(){{
                  $('#infoButton_{$key}').popover({{
                    title: "{mcri18n:translate-with-params('Webpage.docdetails.infodialog.title',
                                (replace(mcri18n:translate($labelkey), '&lt;br /&gt;', '')))}",
                    content : "{replace($info,'"', '&amp;quot;')}",
                    placement :  function(context, src) {{
                        $(context).addClass('po_{$key}');
                        return 'left'; 
                    }},
                    html: true,
                    trigger: 'manual' 
                }}) 
                .on('mouseenter', function () {{
                   $('#infoButton_{$key}').popover('show');
                   $('.po_{$key}').on('mouseleave', function () {{ 
                     $('#infoButton_{$key}').popover('hide');
                   }});  
                }})
                .on('mouseleave', function () {{
                    setTimeout(function () {{
                         if (!$('.po_{$key}:hover').length) {{ 
                           $('#infoButton_{$key}').popover('hide'); 
                         }}
                     }}, 500);
                }});
              }});
            </script>
          </xsl:when>
          <xsl:otherwise>&#160;</xsl:otherwise>
        </xsl:choose>
      </span>
      
      <xsl:if test="$labelkey"> 
      <span id="docdetails-label-{$key}" class="docdetails-label">
        <xsl:value-of select="replace(mcri18n:translate($labelkey),'&lt;br /&gt;','')" disable-output-escaping="true" />:
      </span>
      </xsl:if>
      <div class="docdetails-table-div">
        <table class="docdetails-table {$css_class}">
          <!-- <xsl:copy-of select="$items" /> -->
          <xsl:apply-templates select="$items" mode="output_html"/>
        </table>
      </div>
    </div>
    
  </xsl:template>
  
  <xsl:template name="dd_separator">
    <div class="docdetails-separator">
       <hr />
    </div>
  </xsl:template>
  
  <xsl:template match="text()" mode="output_html">
    <xsl:value-of select="." disable-output-escaping="yes"/>
  </xsl:template>

  <xsl:template match="*|@*" mode="output_html">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="output_html"/>
      <xsl:apply-templates mode="output_html"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
