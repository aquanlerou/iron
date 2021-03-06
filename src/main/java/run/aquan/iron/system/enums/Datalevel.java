package run.aquan.iron.system.enums;

/**
 * @Author Saving
 * @Description 数据级别枚举
 * @Date 2019/12/21 15:15
 * @Param
 * @return
 **/
public enum Datalevel implements ValueEnum<Integer> {

    /**
     * 无效数据
     **/
    UNEFFECTIVE(0),

    /**
     * 有效数据
     **/
    EFFECTIVE(1);

    private final int value;

    Datalevel(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

}

