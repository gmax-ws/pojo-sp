## Calling stored procedures using Java plain objects (POJO)
Author: Marius Gligor  
Contact: marius.gligor@gmail.com  

*Abstract:*

>Today almost all Java applications having persistence layers are designed
using the JPA (Java Persistence API) and/or one of the existing ORM frameworks
like Hibernate, EclipseLink, etc. On the Hibernate Reference Manual preface we are advised
that Hibernate may not be the best solution when we have to work with stored procedures.
"Hibernate may not be the best solution for data-centric applications that only use stored-procedures
to implement the business logic in the database, it is most useful with object-oriented domain models
and business logic in the Java-based middle-tier."

*Solution:*

>If you have to call stored procedures on your applications you have to use the standard JDBC API
working with CallableStatement objects. But now you can use a more simple and efficient solution
using an annotated POJO and a ProcedureManager instance. Designed from scratch the pojo-sp library
offers the best object oriented (OOP) solution to call stored procedures from your applications
by hiding the JDBC SQL programming artefacts. For each stored procedure or function
call you have to create a POJO, a simple Java Bean class with annotations. The POJO class MUST be
decorated with a @StoredProcedure annotation and you have to specify the name of the stored procedure
or function that is mapped by your class.
If the procedure or the function is from an Oracle package you have to specify 
also the name of the package:
```
  <PACKAGE_NAME>.<PROCEDURE_NAME>
```
>Next you have to specify if the entity you are calling is a procedure or a function. This attribute
is by default true for stored procedures and MUST be set to false if the entity you are calling is a
function. The difference between a procedure and a function is that the function always has a return
value. Inside your POJO class you have to define the stored procedure parameters call and decorate
the fields with @StoredProcedureParameter annotation. You have to specify the index of your
parameters starting from 1 for the first parameter. The names of the parameters are not important
because the parameters are accessed by index not by names. If the entity is a function the first parameter
(index = 1) is always the return value. The next attribute is the SQL type of the parameter. Here you have
to be carefully because this is a mapping of a SQL type to a Java type
and this mapping MUST match. The last attribute is the direction for this parameter which
can be IN, OUT or INOUT. For the result parameter of a function you MUST specify always
OUT as a direction attribute.

Hello POJO class example for calling a function:
```
@StoredProcedure(name = "HELLO", procedure = false)
public class Hello {

  @StoredProcedureParameter(index = 1, type = Types.VARCHAR, direction = Direction.OUT)
  private String result;

  @StoredProcedureParameter(index = 2, type = Types.VARCHAR, direction = Direction.IN)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
```
>The next step is to create a ProcedureManager instance, set the input parameters on your POJO
class and finally call the stored procedure. The ProcedureManager instance is created using
the createInstance() method of ProcedureManagerFactory using a DataSource, a JDBC Connection
object as parameter or a class decorated with @JDBC annotation.
If you need to use transactions a TransactionManager interface is available to use. Also all "checked"
exceptions are converted to "unchecked" exceptions and it's not mandatory to use a try catch block on your code.

Example:
```
@JDBC(driver = "oracle.jdbc.OracleDriver", 
  url = "jdbc:oracle:thin:@127.0.0.1:1521:XE", 
  username = "HR", password = "hr")
public class Main {

  public void test() {
    ProcedureManager pm = ProcedureManagerFactory.createInstance(Main.class);
    try {
      pm.getTransactionManager().begin();

      // Call BALANCE procedure
      Balance bal = new Balance();
      bal.setPrice(BigDecimal.valueOf(23.45f));
      bal.setQuantity(BigDecimal.valueOf(123));
      pm.call(bal);
      System.out.println(bal.getVal());

      // Call HELLO function
      Hello hello = new Hello();
      hello.setName("Marius");
      pm.call(hello);
      System.out.println(hello.getResult());

      pm.getTransactionManager().commit();
    } catch (Exception e) {
      pm.getTransactionManager().rollback();
    } finally {
      pm.close();
    }

}
```
>The sample code is designed to use an Oracle XE database connection and two
simple stored procedures HELLO and BALANCE
```
CREATE OR REPLACE
  FUNCTION HELLO (NAME IN VARCHAR2) RETURN VARCHAR2 AS
  BEGIN
    RETURN 'HELLO ' || NAME;
  END HELLO;

CREATE OR REPLACE
  PROCEDURE BALANCE (PARAM1 IN NUMBER, PARAM2 IN NUMBER, PARAM3 OUT NUMBER) AS
  BEGIN
    PARAM3 := PARAM1 * PARAM2;
  END BALANCE;
```
>The pojo-sp library is distributed under the GNU GENERAL PUBLIC LICENSE.
You are welcome to send any questions, improvements ideas, and impressions to the author.

Thanks!
