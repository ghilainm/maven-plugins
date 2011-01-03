package org.apache.maven.plugin.jira;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.maven.plugin.issues.Issue;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML parser that extracts <code>Issue</code>s from JIRA. This works on an XML
 * file downloaded from JIRA and creates a <code>List</code> of issues that is
 * exposed to the user of the class.
 *
 * @version $Id$
 */
public class JiraXML
    extends DefaultHandler
{
    private List issueList;

    private StringBuffer currentElement = new StringBuffer( 1024 );

    private String currentParent = "";

    private Issue issue;

    public JiraXML( File xmlPath, String encoding )
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        FileInputStream fis = null;

        issueList = new ArrayList();

        try
        {
            SAXParser saxParser = factory.newSAXParser();

            fis = new FileInputStream( xmlPath );
            InputSource inputSource = new InputSource( fis );
            inputSource.setEncoding( encoding );

            saxParser.parse( inputSource, this );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        finally
        {
            if ( fis != null )
            {
                try
                {
                    fis.close();
                }
                catch ( IOException e )
                {
                    // Ignore
                }
            }
        }
    }

    public void startElement( String namespaceURI, String sName, String qName, Attributes attrs )
        throws SAXException
    {
        if ( qName.equals( "item" ) )
        {
            issue = new Issue();

            currentParent = "item";
        }
    }

    public void endElement( String namespaceURI, String sName, String qName )
        throws SAXException
    {
        if ( qName.equals( "item" ) )
        {
            issueList.add( issue );

            currentParent = "";
        }
        else if ( qName.equals( "key" ) )
        {
            issue.setKey( currentElement.toString().trim() );
        }
        else if ( qName.equals( "summary" ) )
        {
            issue.setSummary( currentElement.toString().trim() );
        }
        else if ( qName.equals( "type" ) )
        {
            issue.setType( currentElement.toString().trim() );
        }
        else if ( qName.equals( "link" ) && currentParent.equals( "item" ) )
        {
            issue.setLink( currentElement.toString().trim() );
        }
        else if ( qName.equals( "priority" ) )
        {
            issue.setPriority( currentElement.toString().trim() );
        }
        else if ( qName.equals( "status" ) )
        {
            issue.setStatus( currentElement.toString().trim() );
        }
        else if ( qName.equals( "resolution" ) )
        {
            issue.setResolution( currentElement.toString().trim() );
        }
        else if ( qName.equals( "assignee" ) )
        {
            issue.setAssignee( currentElement.toString().trim() );
        }
        else if ( qName.equals( "reporter" ) )
        {
            issue.setReporter( currentElement.toString().trim() );
        }
        else if ( qName.equals( "version" ) && currentParent.equals( "item" ) )
        {
            issue.setVersion( currentElement.toString().trim() );
        }
        else if ( qName.equals( "fixVersion" ) )
        {
            issue.addFixVersion( currentElement.toString().trim() );
        }
        else if ( qName.equals( "component" ) )
        {
            issue.addComponent( currentElement.toString().trim() );
        }
        else if ( qName.equals( "comment" ) )
        {
            issue.addComment( currentElement.toString().trim() );
        }
        else if ( qName.equals( "title" ) && currentParent.equals( "item" ) )
        {
            issue.setTitle( currentElement.toString().trim() );
        }

        currentElement.setLength( 0 );
    }

    public void characters( char[] buf, int offset, int len )
        throws SAXException
    {
        currentElement.append( buf, offset, len );
    }

    public List getIssueList()
    {
        return this.issueList;
    }
}
