## Change Requirements
Add a new entity to the project called BeerOrderShipment. 

The BeerOrderShipment entity has the following properties:
* shipmentDate - not null
* carrier - not null
* carrier number - not null


The BeerOrder entity will have a OneToMany relationship with BeerOrderShipment entity.

Add a flyway migration script for the new BeerOrderShipment JPA Entity.

Add Java DTOs, Mappers, Spring Data Repositories, service and service implementation to support a Spring MVC RESTful 
CRUD controller. Add Tests for all components. Update the OpenAPI documentation for the new controller operations. Verify 
all tests are passing.