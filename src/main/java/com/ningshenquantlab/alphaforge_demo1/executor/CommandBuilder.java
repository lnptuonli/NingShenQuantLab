package com.ningshenquantlab.alphaforge_demo1.executor;
//命令构建器


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Python 命令构建器
 */
@Component
public class CommandBuilder {

    @Value("${python.interpreter}")
    private String pythonPath;

    @Value("${python.script-path}")
    private String scriptPath;

    /**
     * 构建特征计算命令
     * 示例：python feature_manager.py calc --start 20250301 --end 20250305 --force
     */
    public List<String> buildFeatureCalcCommand(
            String startDate, String endDate, boolean force) {

        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(Paths.get(scriptPath, "feature_manager.py").toString());
        command.add("calc");
        command.add("--start");
        command.add(startDate);
        command.add("--end");
        command.add(endDate);

        if (force) {
            command.add("--force");
        }

        return command;
    }
}
