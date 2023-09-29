/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.language.editor.information.examples

import net.akehurst.language.editor.information.Example

object Xml {
    val id = "xml"
    val label = "Xml"
    val sentence = """
<!-- Copied from [https://www.w3schools.com/xml/simple.xml] -->
<breakfast_menu>
    <food>
        <name>Belgian Waffles</name>
        <price>${'$'}5.95</price>
        <description>Two of our famous Belgian Waffles with plenty of real maple syrup</description>
        <calories>650</calories>
    </food>
    <food>
        <name>Strawberry Belgian Waffles</name>
        <price>${'$'}7.95</price>
        <description>Light Belgian waffles covered with strawberries and whipped cream</description>
        <calories>900</calories>
    </food>
    <food>
        <name>Berry-Berry Belgian Waffles</name>
        <price>${'$'}8.95</price>
        <description>Light Belgian waffles covered with an assortment of fresh berries and whipped cream</description>
        <calories>900</calories>
    </food>
    <food>
        <name>French Toast</name>
        <price>${'$'}4.50</price>
        <description>Thick slices made from our homemade sourdough bread</description>
        <calories>600</calories>
    </food>
    <food>
        <name>Homestyle Breakfast</name>
        <price>${'$'}6.95</price>
        <description>Two eggs, bacon or sausage, toast, and our ever-popular hash browns</description>
        <calories>950</calories>
    </food>
</breakfast_menu>
    """.trimIndent()
    val grammar = """
namespace net.akehurst.language.xml

grammar Xml {
	skip leaf WS = "\s" ;

	document = prolog? comment* element ;
	prolog = '<?xml' attribute* '?>' ;
    comment = '<!--' COMMENT_CONTENT '-->' ;
	element = elementEmpty | elementContent ;
	elementEmpty = '<' NAME attribute* '/>' ;
    elementContent = startTag content endTag ;
	startTag = '<'  NAME attribute*  '>' ;
	endTag = '</'  NAME  '>' ;

	content = (CHARDATA | element | comment)* ;

	attribute = NAME '=' VALUE ;

	leaf VALUE = DOUBLE_QUOTE_STRING | SINGLE_QUOTE_STRING ;
	leaf COMMENT_CONTENT = "(.|[\n])*?(?=-->)" ;
	leaf CHARDATA = "[^<]+" ;
	leaf NAME = "[a-zA-Z_:][a-zA-Z0-9_.:-]*" ;
	leaf DOUBLE_QUOTE_STRING = "[\"][^\"]*[\"]" ;
	leaf SINGLE_QUOTE_STRING = "['][^']*[']" ;
}
    """.trimIndent()

    val references = """
    """.trimIndent()

    val style = """
        comment {
          foreground: gray;
          font-style: italic;
        }
        NAME {
          foreground: red;
          font-style: bold; 
        }
        
        CHARDATA {
          foreground: green;
        }
        attribute {
          foreground: blue;
        }
        VALUE {
        foreground: green;
        }
        '<','/>','=' {
          foreground: purple;
        }
    """.trimIndent()
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, references, style, format)

}