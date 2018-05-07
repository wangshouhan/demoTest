package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.cglib.core.Predicate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void test(){
		List<String> features = Arrays.asList("Lambdas", "Default Method", "Stream API", "Date and Time API");
		for (Object feature : features) {
			System.out.println(feature);
		}
	}
	@Test
	public void test1(){
        // 静态方法引用
        List<Integer> ints = Arrays.asList(1, 2, 3);
        ints.sort(Integer::compare);

        //对象的实例方法引用
        List<String> strings = Arrays.asList("1", "2", "3");
        strings.forEach(System.out::println);

        //类的实例方法引用
        strings.stream().map(word -> word.length()); // lambda
        strings.stream().map(String::length); // method reference

        //构造函数引用
        strings.stream().map(StringBuilder::new);
        Integer x = 1;
	}





}
