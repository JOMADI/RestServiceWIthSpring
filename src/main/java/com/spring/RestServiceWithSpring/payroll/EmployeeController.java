package com.spring.RestServiceWithSpring.payroll;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.hateoas.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
public class EmployeeController {


    private final EmployeeRepository  repository;
    private final EmployeeResourceAssembler  assembler;

    public EmployeeController(EmployeeRepository repository, EmployeeResourceAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    //Aggregate root

//    @GetMapping("/employees")
//    Collection<Employee> all(){
//        return repository.findAll();
//    }

    @GetMapping(path="/employees", produces=MediaType.APPLICATION_JSON_VALUE)
    Resources<Resource<Employee>> all(){
        Collection<Resource<Employee>> employees = repository.findAll()
                .stream()
                .map(assembler::toResource)
                .collect(Collectors.toList());

        return new Resources<>(employees,
                linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }


//    @PostMapping("/employees")
//    Employee newEmployee(@RequestBody Employee newEmployee){
//        return repository.save(newEmployee);
//    }

    //Post that handles both old and new clients
    @PostMapping("/employees")
    ResponseEntity<?> newEmployee(@RequestBody Employee newEmployee) throws URISyntaxException {
        Resource<Employee> resource = assembler.toResource(repository.save(newEmployee));

        return ResponseEntity.created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    //Single Item

//    @GetMapping("/employees/{id}")
//    Employee one(@PathVariable Long id){
//        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
//    }

    @GetMapping(path="/employees/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
    Resource<Employee> one(@PathVariable Long id){
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return assembler.toResource(employee);
    }

//    @PutMapping("/employees/{id}")
//    Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id){
//        return repository.findById(id)
//                .map(employee -> {
//                    employee.setName(newEmployee.getName());
//                    employee.setRole((newEmployee.getRole()));
//                    return repository.save(employee);
//                })
//                .orElseGet(() -> {
//                    newEmployee.setId((id));
//                    return repository.save(newEmployee);
//                });
//    }

    @PutMapping("/employees/{id}")
    ResponseEntity<?> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) throws URISyntaxException {

        Employee updatedEmployee = repository.findById(id)
                .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return repository.save(employee);
                })
                .orElseGet(() -> {
                    newEmployee.setId(id);
                    return repository.save(newEmployee);
                });

        Resource<Employee> resource = assembler.toResource(updatedEmployee);

        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    @DeleteMapping("/employees/{id}")
    void deleteEmployee(@PathVariable Long id){
        repository.deleteById(id);
    }


}
