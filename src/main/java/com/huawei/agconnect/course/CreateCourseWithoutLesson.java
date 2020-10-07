
package com.huawei.agconnect.course;

import com.huawei.agconnect.callback.ProgressCallbackImpl;
import com.huawei.agconnect.server.commons.AGCClient;
import com.huawei.agconnect.server.commons.AGCParameter;
import com.huawei.agconnect.server.commons.credential.CredentialParser;
import com.huawei.agconnect.server.commons.exception.AGCException;
import com.huawei.agconnect.server.edukit.AGCEdukit;
import com.huawei.agconnect.server.edukit.common.constant.CommonConstants;
import com.huawei.agconnect.server.edukit.common.errorcode.CommonErrorCode;
import com.huawei.agconnect.server.edukit.common.model.ImageFileInfo;
import com.huawei.agconnect.server.edukit.common.model.MediaFileInfo;
import com.huawei.agconnect.server.edukit.common.model.MediaLocalizedData;
import com.huawei.agconnect.server.edukit.common.model.ProductPrice;
import com.huawei.agconnect.server.edukit.course.impl.CourseCreateRequest;
import com.huawei.agconnect.server.edukit.course.model.Course;
import com.huawei.agconnect.server.edukit.course.model.CourseEditMetaData;
import com.huawei.agconnect.server.edukit.course.model.CourseLocalizedData;
import com.huawei.agconnect.server.edukit.course.model.CourseMetaData;
import com.huawei.agconnect.server.edukit.course.model.CourseMultiLanguageData;
import com.huawei.agconnect.server.edukit.course.resp.CourseCreateResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class CreateCourseWithoutLesson {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCourseWithoutLesson.class);

    public static void main(String[] args) {
        String clientName = "edukit";
        try {
            AGCClient.initialize(clientName,
                AGCParameter.builder()
                    .setCredential(CredentialParser.toCredential(
                        CreateCourseWithoutLesson.class.getClassLoader().getResource("credential.json").getPath()))
                    .build());
        } catch (AGCException e) {
            // 用户可以做记录日志，抛异常等处理
            return;
        }

        Course course = buildCourse();
        CourseCreateRequest courseCreateRequest = AGCEdukit.getInstance(clientName).getCourseCreateRequest(course);
        CompletableFuture<CourseCreateResponse> future = courseCreateRequest.commit();

        // latch仅用于demo中防止main线程提前结束导致进程退出，在您的工程中不需要使用Latch进行同步操作
        CountDownLatch waitTaskLatch = new CountDownLatch(1);
        future.thenAccept(courseCreateResponse -> {
            if (courseCreateResponse.getResult().getResultCode() == CommonErrorCode.SUCCESS) {
                // 课程创建成功后，需要在本地保存返回的courseId和courseEditId
                // 保存课程Id与您的课程的关联关系
                // 保存课程版本ID，如果审核没有通过，可以使用该课程版本ID继续更新
            } else {
                LOGGER.error("Create course response : {}", courseCreateResponse);
                // 根据错误码进行异常场景处理
            }
            waitTaskLatch.countDown();
        });
        // 以下操作非demo示例，仅为了保证在异步任务执行完成前main线程不会因为执行完成导致程序提前结束
        try {
            waitTaskLatch.await();
            LOGGER.info("Create course finished.");
        } catch (InterruptedException e) {
            LOGGER.error("Create course without lesson failed.");
        }
    }

    private static Course buildCourse() {
        List<String> tagIds = new ArrayList<>();
        List<String> categoryIds = new ArrayList<>();
        List<String> countryCodes = new ArrayList<>();
        categoryIds.add("214338295549722624");
        tagIds.add("243338018147009280");
        countryCodes.add("CN");
        CourseEditMetaData courseEditMetaData = CourseEditMetaData.builder()
            .defaultLangSet("zh-CN")
            .nameSet("课程一")
            .tagIdsSet(tagIds)
            .validityUnitSet(1)
            .includeLessonsSet(false)
            .eduappPurchasedSet(true)
            .distNotifyUrlSet("https://test.com")
            .sourceNameSet("my app")
            .eduappUsedSet(false)
            .sourceNameSet("My app")
            .sellingModeSet(3)
            .categoryIdsSet(categoryIds)
            .typeIdSet(1000)
            .build();
        CourseMetaData courseMetaData =
            CourseMetaData.builder().courseEditMetaDataSet(courseEditMetaData).countryCodesSet(countryCodes).build();

        List<ImageFileInfo> introduceImageFileInfoSet = new ArrayList<>();
        ImageFileInfo cover = ImageFileInfo.builder()
            // 课程封面 jpg、png格式，图片分辨率为1280*720像素(宽*高)，单张图片最大为2MB
            .pathSet(CreateCourseWithoutLesson.class.getClassLoader().getResource("cover.PNG").getPath())
            .resourceTypeSet(CommonConstants.ResourceType.COURSE_HORIZONTAL_COVER)
            .build();
        CourseLocalizedData courseLocalizedData = CourseLocalizedData.builder()
            .nameSet("课程一")
            .shortDescriptionSet("SDK课程简介")
            .fullDescriptionSet("SDK课程详细介绍")
            .coverImageFileInfoSet(cover)
            .deeplinkUrlSet("www.yourapp.com")
            .deeplinkLenSet(100)
            .build();

        CourseMultiLanguageData courseMultiLanguageData = CourseMultiLanguageData.builder()
            .courseLocalizedDataSet(courseLocalizedData)
            .languageSet("zh-CN")
            .build();

        List<CourseMultiLanguageData> courseLocalizedDataList = new ArrayList<>();
        courseLocalizedDataList.add(courseMultiLanguageData);

        List<ProductPrice> productPricesSet = new ArrayList<>();
        ProductPrice productPrice = ProductPrice.builder().priceSet(1.0).countryCodeSet("CN").priceTypeSet(2).build();
        productPricesSet.add(productPrice);

        return Course.builder()
            .courseMetaDataSet(courseMetaData)
            .courseMultiLanguageDataListSet(courseLocalizedDataList)
            .productPricesSet(productPricesSet)
            .progressCallbackSet(new ProgressCallbackImpl()::onProgressChanged)
            .build();

    }
}
