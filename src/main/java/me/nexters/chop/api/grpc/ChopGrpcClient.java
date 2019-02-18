package me.nexters.chop.api.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import me.nexters.chop.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author junho.park
 */
@Component
public class ChopGrpcClient {
    private static Logger logger = LoggerFactory.getLogger(ChopGrpcClient.class);

    private UrlClickServiceGrpc.UrlClickServiceStub urlClickStub;
    private UrlStatsServiceGrpc.UrlStatsServiceBlockingStub urlStatsServiceBlockingStub;

    public ChopGrpcClient(Channel channel) {
        urlStatsServiceBlockingStub = UrlStatsServiceGrpc.newBlockingStub(channel);
        urlClickStub = UrlClickServiceGrpc.newStub(channel);
    }

    public void insertStatsToStatsServer(String shortenUrl, String referer, String userAgent) {
        Url url = Url.newBuilder().setShortUrl(shortenUrl)
                .setClickTime(generateCurrentTimestamp())
                .setPlatform(userAgent)
                .setReferer(referer).build();

        logger.info("클라이언트 측에서 클릭 정보 전송");

        urlClickStub.unaryRecordCount(url, new StreamObserver<Success>() {
            @Override
            public void onNext(Success success) {
                logger.info(success.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("Grpc 서버 응답 종료");
            }
        });
    }

    private Timestamp generateCurrentTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        return Timestamp.newBuilder().
            setSeconds(currentTimeMillis / 1000)
            .setNanos((int) ((currentTimeMillis % 1000) * 1000000)).build();
    }

    public Platform getPlatformStats(String shortenUrl) {
        UrlStatsRequest urlStatsRequest = UrlStatsRequest.newBuilder()
                .setShortUrl(shortenUrl)
                .build();

        Platform platform = null;

        try {
            platform = urlStatsServiceBlockingStub.getPlatformCount(urlStatsRequest);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new EntityNotFoundException(e.getMessage());
            }
        }

        return platform;
    }

    public List<Referer> getRefererStats(String shortenUrl) {
        UrlStatsRequest urlStatsRequest = UrlStatsRequest.newBuilder()
                .setShortUrl(shortenUrl)
                .build();

        List<Referer> referers = new ArrayList<>();
        Iterator<Referer> refererIterator;

        try {
            refererIterator = urlStatsServiceBlockingStub.getRefererCount(urlStatsRequest);

            while (refererIterator.hasNext()) {
                referers.add(refererIterator.next());
            }

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new EntityNotFoundException(e.getMessage());
            }
        }

        return referers;
    }

    public TotalCount getTotalCount(String shortenUrl) {
        UrlStatsRequest urlStatsRequest = UrlStatsRequest.newBuilder()
                .setShortUrl(shortenUrl)
                .build();

        TotalCount totalCount = null;

        try {
            totalCount = urlStatsServiceBlockingStub.getTotalCount(urlStatsRequest);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new EntityNotFoundException(e.getMessage());
            }
        }

        return totalCount;
    }

    public List<ClickCount> getClickCount(String shortenUrl, int week) {
        UrlClickStatsRequest urlClickStatsRequest = UrlClickStatsRequest.newBuilder()
                .setShortUrl(shortenUrl)
                .setWeek(week)
                .build();

        List<ClickCount> clickCounts = new ArrayList<>();
        Iterator<ClickCount> clickCountIterator;

        try {
            clickCountIterator = urlStatsServiceBlockingStub.getClickCount(urlClickStatsRequest);

            while (clickCountIterator.hasNext()) {
                clickCounts.add(clickCountIterator.next());
            }
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new EntityNotFoundException(e.getMessage());
            }
        }

        return clickCounts;
    }
}
