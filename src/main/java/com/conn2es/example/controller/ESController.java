package com.conn2es.example.controller;

import com.conn2es.example.service.ESService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.GET;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Controller
public class ESController {

    @Autowired
    private ESService esService;

    @ResponseBody
    @PostMapping(value = "/{index}/search")
    public ArrayList<Map<String, Object>> search(@PathVariable("index") String index) throws IOException {
        return esService.search(index);
    }
}
