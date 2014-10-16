package test.jason;

public class Professor implements Cloneable    
{    
     String name;    
     int age;    
     Professor(String name,int age)    
     {    
        this.name=name;    
        this.age=age;    
     }    
//    public Object clone()    
//     {    
//         Object o=null;    
//        try    
//         {    
//             o=super.clone();    
//         }    
//        catch(CloneNotSupportedException e)    
//         {    
//             System.out.println(e.toString());    
//         }    
//        return o;    
//     }    
//    
    
    public static void main(String[] args)    
    {    
      Professor p=new Professor("wangwu",50);    
      Student s1=new Student("zhangsan",18,p);    
      Student s2=(Student)s1.clone();    
      s2.p.name="lisi";    
      s2.p.age=30;    
      System.out.println("name="+s1.p.name+","+"age="+s1.p.age);//学生1的教授不 改变。    \
      
      try {
		Professor p2 = (Professor) p.clone();
		System.out.println(p2==p);
		System.out.println(p2.equals(p));
		System.out.println(p.toString()+"  "+p2.toString());
	} catch (CloneNotSupportedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }    


}    
class Student implements Cloneable    
{    
     String name;    
     int age;    
     Professor p;    
     Student(String name,int age,Professor p)    
     {    
        this.name=name;    
        this.age=age;    
        this.p=p;    
     }    
    public Object clone()    
     {    
         Student o=null;    
        try    
         {    
             o=(Student)super.clone();    
         }    
        catch(CloneNotSupportedException e)    
         {    
             System.out.println(e.toString());    
         }    
        // o.p=(Professor)p.clone();    
        return o;    
     }    
}

