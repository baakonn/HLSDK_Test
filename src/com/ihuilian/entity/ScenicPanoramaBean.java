package com.ihuilian.entity;

import java.util.List;

/**
 * 景区所有全景的集合
 * author:bakon(762713299@qq.com)
 * date:2016/5/12-10:54
 */
public class ScenicPanoramaBean {
    public List<PanoramaBean> list;

    public class PanoramaBean {
        //全景id
        public String id;
        //全景名称
        public String name;
        //全景类型（立方体、球体）
        public int type;
        //全景分辨率（512、1024、2048、4096）
        public int resolution;
        //全景描述
        public String description;
        //全景md5
        public String md5;
        //全景url（hash）
        public String url;

        public Sources sources;

        public class Sources {
            //全景原图url
            public String orgin;
            //全景缩略图url
            public String thumb;
            //球体形式
            public Sphere sphere;
        /*//立方体形式
        public List<Cube> cube;*/

        }

        /* //立方体
         public class Cube{
             public Cube_titles tiles;
             public Cube_512 cube_512;
             public Cube_1024 cube_1024;
             public Cube_2048 cube_2048;
             public Cube_4096 cube_4096;
         }*/
        //球体
        public class Sphere {
            public Sphere_titles tiles;
            public Sphere_512 sphere_512;
            public Sphere_1024 sphere_1024;
            public Sphere_2048 sphere_2048;
            public Sphere_4096 sphere_4096;
        }

        public class Sphere_titles {
            public String sphere_512;
            public String sphere_1024;
            public String sphere_2048;
            public String sphere_4096;
        }

        public class Sphere_512 {
            //底质量url
            public String low;
            //中质量url
            public String medium;
            //高质量url
            public String high;
        }

        public class Sphere_1024 {
            //底质量url
            public String low;
            //中质量url
            public String medium;
            //高质量url
            public String high;
        }

        public class Sphere_2048 {
            //底质量url
            public String low;
            //中质量url
            public String medium;
            //高质量url
            public String high;
        }

        public class Sphere_4096 {
            //底质量url
            public String low;
            //中质量url
            public String medium;
            //高质量url
            public String high;
        }
    /*public class Cube_titles{
        public String cube_512;
        public String cube_1024;
        public String cube_2048;
        public String cube_4096;
    }
    public class Cube_512{
        //底质量url
        public String low;
        //中质量url
        public String medium;
        //高质量url
        public String high;
    }
    public class Cube_1024{
        //底质量url
        public String low;
        //中质量url
        public String medium;
        //高质量url
        public String high;
    }
    public class Cube_2048{
        //底质量url
        public String low;
        //中质量url
        public String medium;
        //高质量url
        public String high;
    }
    public class Cube_4096{
        //底质量url
        public String low;
        //中质量url
        public String medium;
        //高质量url
        public String high;
    }*/
    }


}

