package co.epitre.aelf_lectures.lectures.data;

import com.squareup.moshi.FromJson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class LectureVariantsJsonAdapter {
    @FromJson
    LectureVariants lectureVariantsFromJson(List<Lecture> lectureVariantsFromJson) {
        return new LectureVariants(lectureVariantsFromJson);
    }
}

public class LectureVariants implements Serializable {
    List<Lecture> mLectures;
    transient List<String> mTitles;

    public LectureVariants(List<Lecture> lectures) {
        this.mLectures = lectures;
    }

    public Lecture get(int variant) {
        return mLectures.get(variant);
    }

    public boolean hasVariants() {
        return mLectures.size() > 1;
    }

    public List<String> getVariantTitles() {
        if (mTitles != null) {
            return mTitles;
        }

        mTitles = new ArrayList<>(mLectures.size());
        for (Lecture lecture: mLectures) {
            mTitles.add(lecture.getVariantTitle());
        }

        return mTitles;
    }
}
