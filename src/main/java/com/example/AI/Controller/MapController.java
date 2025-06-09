package com.example.AI.Controller;

import com.example.AI.DTO.MapRequest;
import com.example.AI.Service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "http://localhost:63342")
public class MapController {
    @Autowired
    private MapService mapService;
    @PostMapping("/createMap")
    public ResponseEntity<String> createMap(@RequestBody MapRequest request){
        return ResponseEntity.ok(mapService.createMap(request));
    }
    @PostMapping("/saveLocation")
    public ResponseEntity<String> saveLocation(@RequestBody MapRequest request){
        return ResponseEntity.ok(mapService.saveLocation(request));
    }
    @PostMapping("/findRoute")
    public ResponseEntity<String> findRoute(){
        return ResponseEntity.ok(mapService.findRoute());
    }
}
