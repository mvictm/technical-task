<h1>#Technical Task for Exactpro company.<h1/> 
    
This project realize function of serialization/deserialization JavaBeans. 
<p> Firstly, I need to implement the interface:</p>

```java
interface SuperEncoder {
          byte[] serialize(Object anyBean);
          Object deserialize(byte[] data);
          }
 ```
<p>Secondly, JavaBeans have to implement <code>Serializable</code> interface.</p> 
<p>Moreover, JavaBeans can keep fields, such as:</p> 

<ul>primitives - <code>int</code>, <code>short</code>;</ul>
<ul>wrapper - <code>Integer</code>, <code>Long</code>;</ul> 
<ul>Collections - <code>List</code>, <code>Map</code> and ect.</ul> 

<p>Also, if a class has a cyclic reference, they generate exceptions.</p>
<p>In this project I used the logger(log4j2) and project lombok (accept create getter/setter, override <code>toString()</code>
and another methods by means of annotations). I used Apache Maven for building the project.</p> 
