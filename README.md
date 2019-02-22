<h1>#Technical Task for Exactpro company.<h1/> 
    
<big>This project realize function of serialization/deserialization JavaBeans. 
Firstly, I need to implement the interface: <big/>
<pre>interface SuperEncoder {
          byte[] serialize(Object anyBean);
          Object deserialize(byte[] data);
          }
 </pre>
Secondly, JavaBeans have to implement <code>Serializable</code> interface. Moreover, JavaBeans can keep fields, such as: 
primitives - <code>int</code>, <code>short</code>; wrapper - <code>Integer</code>, <code>Long</code>; Collections - 
<code>List</code>, <code>Map</code> and ect. Also, if a class has a cyclic reference, they generate 
 exceptions.
 
In this project I used the logger(log4j2) and project lombok (accept create getter/setter, override <code>toString()</code>
and another methods by means of annotations). I used Apache Maven for building the project. 
