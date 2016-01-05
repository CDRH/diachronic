<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
   xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:ge="http://www.tei-c.org/ns/geneticEditions"
   xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:diachronic="http://diachronic">

   <!-- ################# This XSLT stylesheet was created by Brian L. Pytlik Zillig ################# -->
   <!-- ################# February 24, 2015 ################################################### -->

   <xsl:output encoding="utf-8" indent="yes" omit-xml-declaration="yes" method="xhtml"/>

   <xsl:strip-space elements="*"/>

   <xsl:template match="tei:TEI">
      <html xmlns="http://www.w3.org/1999/xhtml">
         <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
            <title>Diachronic Markup: a prototype</title>
            <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"/>
            <script src="../javascript/show-hide.js"/>
            <link rel="StyleSheet" href="../css/diachronic.css" type="text/css"> </link>
         </head>
         <body>

            <div class="body">
               <xsl:for-each select="//tei:titleStmt//tei:title[1]">
                  <div class="header">
                     <xsl:value-of select="."/>
                     <xsl:text> (</xsl:text>
                     <xsl:value-of select="//tei:idno"/>
                     <xsl:text>) </xsl:text>
                  </div>
                  <div class="header">An interactive diachronic transcription </div>
               </xsl:for-each>
            </div>

            <div class="body">
               <br/>
               <br/>
               <br/>
               <br/>
               <button id="undo" type="button">Undo</button>
               <xsl:apply-templates/>
            </div>
         </body>
      </html>
   </xsl:template>

   <xsl:template match="tei:teiHeader"/>

   <xsl:template match="tei:add">

      <xsl:variable name="id">
         <xsl:value-of select="generate-id()"/>
      </xsl:variable>

      <xsl:variable name="changeID">
         <xsl:value-of
            select="
               if (replace(replace(@change, '#', ''), 'ch', '') = '')
               then
                  0
               else
                  xs:integer(replace(replace(@change, '#', ''), 'ch', ''))"
         />
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="@place = 'above' and @rend = 'unmarked'">
            <span id="{$id}" class="add"
               title="{local-name()} {@rend}{if (@change != '') 
               then concat(': ',//tei:change[@xml:id = xs:string($changeID)]/tei:desc/text()) else ''}"
               xmlns="http://www.w3.org/1999/xhtml">

               <xsl:if test="@change">
                  <xsl:attribute name="change">
                     <xsl:value-of select="$changeID"/>
                  </xsl:attribute>
               </xsl:if>
               <xsl:apply-templates/>
            </span>
         </xsl:when>

         <xsl:when test="@place = 'before' and @rend = 'unmarked'">
            <xsl:apply-templates/>
         </xsl:when>

         <xsl:when test="@place = 'over' and @rend = 'overwrite'">
            <span id="{$id}" class="add_{@rend}"
               title="{local-name()} {@rend}{if (@change != '') 
               then concat(': ',//tei:change[@xml:id = xs:string($changeID)]/tei:desc/text()) else ''}"
               xmlns="http://www.w3.org/1999/xhtml">

               <xsl:if test="@change">
                  <xsl:attribute name="change">
                     <xsl:value-of select="$changeID"/>
                  </xsl:attribute>
               </xsl:if>

               <xsl:apply-templates/>
            </span>
         </xsl:when>

         <xsl:when test="@place = 'inline' and @rend = 'unmarked'">
            <xsl:apply-templates/>
         </xsl:when>

         <xsl:when test="@place = 'above' and @rend = 'insertion'">
            <span id="{$id}" class="{@place}_{@rend}"
               title="{local-name()} {@rend} with caret{if (@change != '') 
               then concat(': ',//tei:change[@xml:id = xs:string($changeID)]/tei:desc/text()) else ''}"
               xmlns="http://www.w3.org/1999/xhtml">

               <xsl:if test="@change">
                  <xsl:attribute name="change">
                     <xsl:value-of select="$changeID"/>
                  </xsl:attribute>
               </xsl:if>

               <xsl:apply-templates/>
            </span>
         </xsl:when>

         <xsl:otherwise>
            <span id="{$id}" class="error" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:text> [ERR: </xsl:text>
               <xsl:value-of select="local-name()"/>
               <xsl:for-each select="@*">
                  <xsl:sort select="." data-type="text" order="ascending"/>
                  <xsl:text>/@</xsl:text>
                  <xsl:value-of select="name()"/><xsl:text>=</xsl:text>
                  <xsl:value-of select="."/>
               </xsl:for-each> {{<xsl:value-of select="normalize-space(.)"/>}}
               <xsl:text>!] </xsl:text>
            </span>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template match="tei:del">

      <xsl:variable name="id">
         <xsl:value-of select="generate-id()"/>
      </xsl:variable>

      <xsl:variable name="changeID">
         <xsl:value-of
            select="
               if (replace(replace(@change, '#', ''), 'ch', '') = '')
               then
                  0
               else
                  xs:integer(replace(replace(@change, '#', ''), 'ch', ''))"
         />
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="@rend = 'overwrite'">
            <span id="{$id}" class="{@rend}"
               title="{local-name()} {@rend}{if (@change != '') 
               then concat(': ',//tei:change[@xml:id = xs:string($changeID)]/tei:desc/text()) else ''}"
               xmlns="http://www.w3.org/1999/xhtml">

               <xsl:if test="@change">
                  <xsl:attribute name="change">
                     <xsl:value-of select="$changeID"/>
                  </xsl:attribute>
               </xsl:if>

               <xsl:apply-templates/>
            </span>
         </xsl:when>
         <xsl:when test="@rend = 'overstrike'">
            <xsl:variable name="id">
               <xsl:value-of select="generate-id()"/>
            </xsl:variable>
            <span id="{$id}" class="{@rend}"
               title="{local-name()} {@rend}{if (@change != '') 
               then concat(': ',//tei:change[@xml:id = xs:string($changeID)]/tei:desc/text()) else ''}"
               xmlns="http://www.w3.org/1999/xhtml">

               <xsl:if test="@change">
                  <xsl:attribute name="change">
                     <xsl:value-of select="$changeID"/>
                  </xsl:attribute>
               </xsl:if>

               <xsl:apply-templates/>
            </span>
            <xsl:text> </xsl:text>
         </xsl:when>
         <xsl:otherwise>
            <span id="{$id}" class="error" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:text> [ERR_</xsl:text>
               <xsl:value-of select="local-name()"/>
               <xsl:for-each select="@*">
                  <xsl:sort select="." data-type="text" order="ascending"/>
                  <xsl:text>/@</xsl:text>
                  <xsl:value-of select="name()"/><xsl:text>=</xsl:text>
                  <xsl:value-of select="."/>
               </xsl:for-each> {{<xsl:value-of select="normalize-space(.)"/>}}
               <xsl:text>!] </xsl:text>
            </span>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template match="tei:l">

      <xsl:variable name="changeID">
         <xsl:value-of
            select="
               if (replace(replace(@change, '#', ''), 'ch', '') = '')
               then
                  0
               else
                  xs:integer(replace(replace(@change, '#', ''), 'ch', ''))"
         />
      </xsl:variable>

      <xsl:variable name="id">
         <xsl:value-of select="generate-id()"/>
      </xsl:variable>
      <div class="line" xmlns="http://www.w3.org/1999/xhtml">

         <xsl:if test="@change">
            <xsl:attribute name="change">
               <xsl:value-of select="$changeID"/>
            </xsl:attribute>
         </xsl:if>

         <xsl:apply-templates/>
      </div>
   </xsl:template>

   <xsl:template match="tei:seg">
      <xsl:variable name="id">
         <xsl:value-of select="generate-id()"/>
      </xsl:variable>

      <xsl:variable name="changeID">
         <xsl:value-of
            select="
               if (replace(replace(@change, '#', ''), 'ch', '') = '')
               then
                  0
               else
                  xs:integer(replace(replace(@change, '#', ''), 'ch', ''))"
         />
      </xsl:variable>

      <span id="{$id}" class="seg" xmlns="http://www.w3.org/1999/xhtml">

         <xsl:if test="@change">
            <xsl:attribute name="change">
               <xsl:value-of select="$changeID"/>
            </xsl:attribute>
         </xsl:if>

         <xsl:apply-templates/>
      </span>
   </xsl:template>

   <xsl:template match="tei:reg[parent::tei:choice]">
      <xsl:text>[</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>]</xsl:text>
   </xsl:template>

   <xsl:template match="tei:subst">

      <xsl:variable name="changeID">
         <xsl:value-of
            select="
               if (replace(replace(@change, '#', ''), 'ch', '') = '')
               then
                  0
               else
                  xs:integer(replace(replace(@change, '#', ''), 'ch', ''))"
         />
      </xsl:variable>

      <xsl:variable name="id">
         <xsl:value-of select="generate-id()"/>
      </xsl:variable>
      <span id="{$id}" class="subst" xmlns="http://www.w3.org/1999/xhtml">

         <xsl:if test="@change">
            <xsl:attribute name="change">
               <xsl:value-of select="$changeID"/>
            </xsl:attribute>
         </xsl:if>

         <xsl:apply-templates/>
      </span>
   </xsl:template>

   <xsl:template match="tei:sourceDoc | tei:line"/>

</xsl:stylesheet>
