package com.example.kybatch.job.csv;

import com.example.kybatch.domain.user.User;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class CsvUserReader {

    public FlatFileItemReader<User> reader(){
        FlatFileItemReader<User> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("input/user.csv"));
        reader.setLinesToSkip(1);

        DefaultLineMapper<User> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("name", "email", "status");

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            String name = fieldSet.readString("name");
            String email = fieldSet.readString("email");
            String status = fieldSet.readString("status");
            return new User(name, email, status);

        });

        reader.setLineMapper(lineMapper);
        return reader;

    }
}
