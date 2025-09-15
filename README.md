 # # Endpoints en orden de prioridad segun lógica de relaciones:

 1. Crear una lista de sedes: [POST] http://localhost:8080/headquarters/initializeHeadquarters
 2. Crear una lista de maestros: [POST] http://localhost:8080/teachers/initializeTeachers 
 3. Crear una lista de cursos: [POST] http://localhost:8080/courses/initializeCourses 
 
 
 # # Para hacer correr el backend con MySQL:
 
 a) Crear una scheme con el nombre mobile_back (tal como esta escrito en el archivo application.properties en la carpeta de rsources dentro de la carpeta main)
 
 b) Dento del archivo application.properties, modificar las lineas 7 y 8: 
 
        spring.datasource.username=root <--- aqui va tu usuario de MySql
        spring.datasource.password=****** <---- aqui va tu contraseña de MySql
        
   Con estos ajustes, las tablas se crean automaticamente dentro del scheme creado una vez que haces correr el backend.
    
c) En tu cuenta de google, ir a manage your google account > Security > How you sign in to Google > 2-Step Verification. Aqui habilitar esta "verificación doble" y entrar a App Passwords.

   Aqui seguir las instrucciones para crear una app password, que va a contener 16 caracteres separados en tres espacios. 
   
   En caso de que "App Passwords" no aparezca por ningun lado, asegurarse de que tengas habilitada la doble verificación, y luego buscar en el navegador de google "App passwords gmail" 
   (App Passwords - Sign in - Google Accounts). Aqui crear la password de 16 caracteres.
   
d) Para el envio del código al mail (y para completar el procedimiento de registro de usuario), dentro del archivo application.properties, modificar las lineas 21 y 22:

        spring.mail.username=jlazartelagos@gmail.com <--- aqui va tu cuenta de gmail
        spring.mail.password=bcoj ldet lehu zqbx <--- aqui va un password de 16 caracteres.

# # Endpoints y bodies para el postman:

 POST http://localhost:8080/auth/iniciar-registro
 
 {
    "email":"jlazartelagos@gmail.com", <--- reemplazar por tu cuenta de gmail
    "password":"123abc"
}

POST http://localhost:8080/auth/finalizar-registro

{
    "email":"jlazartelagos@gmail.com", <--- reemplazar por tu cuenta de gmail, como en el paso anterior    
    "code": "5772" <--- reemplazar con el código que te llego a tu correo de gmail
}

POST http://localhost:8080/auth/authenticate

{
    "email":"jlazartelagos@gmail.com",  <--- reemplazar por tu cuenta de gmail, como en el paso anterior  
    "password": "123abc"
}
