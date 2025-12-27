package net.fenerbahcesozluk.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fenerbahcesozluk.entity.Entry;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.enums.Role;
import net.fenerbahcesozluk.repository.EntryRepository;
import net.fenerbahcesozluk.repository.TopicRepository;
import net.fenerbahcesozluk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

        private final UserRepository userRepository;
        private final TopicRepository topicRepository;
        private final EntryRepository entryRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional
        public void run(String... args) {
                // Eğer veri varsa atla
                if (userRepository.count() > 0) {
                        log.info("Veriler zaten mevcut, DataLoader atlanıyor.");
                        return;
                }

                log.info("Test verileri yükleniyor...");

                // Kullanıcılar
                User admin = userRepository.save(User.builder()
                                .username("admin")
                                .email("admin@fenerbahcesozluk.net")
                                .password(passwordEncoder.encode("Admin123!"))
                                .role(Role.ADMIN)
                                .build());

                User user1 = userRepository.save(User.builder()
                                .username("sari_kanarya")
                                .email("sari@fenerbahce.com")
                                .password(passwordEncoder.encode("Test123!"))
                                .role(Role.USER)
                                .build());

                User user2 = userRepository.save(User.builder()
                                .username("lacivert_aslan")
                                .email("lacivert@fenerbahce.com")
                                .password(passwordEncoder.encode("Test123!"))
                                .role(Role.USER)
                                .build());

                User user3 = userRepository.save(User.builder()
                                .username("kadikoy_ruhu")
                                .email("kadikoy@fenerbahce.com")
                                .password(passwordEncoder.encode("Test123!"))
                                .role(Role.USER)
                                .build());

                log.info("4 kullanıcı oluşturuldu");

                // Başlıklar
                Topic topic1 = topicRepository.save(Topic.builder()
                                .title("fenerbahçe")
                                .author(admin)
                                .entryCount(3)
                                .viewCount(1250L)
                                .build());

                Topic topic2 = topicRepository.save(Topic.builder()
                                .title("jose mourinho")
                                .author(user1)
                                .entryCount(2)
                                .viewCount(856L)
                                .build());

                Topic topic3 = topicRepository.save(Topic.builder()
                                .title("alex de souza")
                                .author(user2)
                                .entryCount(2)
                                .viewCount(2340L)
                                .build());

                Topic topic4 = topicRepository.save(Topic.builder()
                                .title("fenerbahçe basketbol")
                                .author(user1)
                                .entryCount(1)
                                .viewCount(432L)
                                .build());

                Topic topic5 = topicRepository.save(Topic.builder()
                                .title("kadıköy")
                                .author(user3)
                                .entryCount(2)
                                .viewCount(678L)
                                .build());

                log.info("5 başlık oluşturuldu");

                // Entry'ler
                // fenerbahçe başlığı
                entryRepository.save(Entry.builder()
                                .content(
                                                "türkiye'nin en büyük spor kulübü. sadece futbol değil, basketbol, voleybol ve daha birçok branşta başarılarıyla tanınan, 1907'den beri spor tarihine damga vuran efsane kulüp.")
                                .topic(topic1)
                                .author(user1)
                                .likeCount(342)
                                .build());

                entryRepository.save(Entry.builder()
                                .content("avrupa'nın en köklü kulüplerinden biri. 117 yıllık tarihiyle türk sporunun lokomotifi.")
                                .topic(topic1)
                                .author(user2)
                                .likeCount(234)
                                .build());

                entryRepository.save(Entry.builder()
                                .content("benim için sadece bir kulüp değil, bir yaşam biçimi. her maç günü ayrı bir heyecan.")
                                .topic(topic1)
                                .author(user3)
                                .likeCount(189)
                                .build());

                // jose mourinho başlığı
                entryRepository.save(Entry.builder()
                                .content(
                                                "the special one. fenerbahçe'ye geldiğinden beri takıma bambaşka bir hava katan, taraftarın gönlünü kazanan teknik direktör.")
                                .topic(topic2)
                                .author(user2)
                                .likeCount(567)
                                .build());

                entryRepository.save(Entry.builder()
                                .content(
                                                "şampiyonlar ligi, premier lig, serie a, la liga şampiyonlukları olan dünya çapında bir teknik adam.")
                                .topic(topic2)
                                .author(user1)
                                .likeCount(445)
                                .build());

                // alex de souza başlığı
                entryRepository.save(Entry.builder()
                                .content(
                                                "fenerbahçe'nin efsane 10 numarası. attığı goller, verdiği asistler, liderliği... türk futbolunun gördüğü en iyi yabancı oyunculardan biri.")
                                .topic(topic3)
                                .author(user1)
                                .likeCount(892)
                                .build());

                entryRepository.save(Entry.builder()
                                .content("kaptan. lider. efsane. alex sadece bir futbolcu değildi, fenerbahçe'nin ta kendisiydi.")
                                .topic(topic3)
                                .author(user3)
                                .likeCount(756)
                                .build());

                // basketbol başlığı
                entryRepository.save(Entry.builder()
                                .content(
                                                "euroleague şampiyonluğu, sayısız final four'lar... fenerbahçe basketbol takımı avrupa'nın en iyi takımlarından biri.")
                                .topic(topic4)
                                .author(user2)
                                .likeCount(456)
                                .build());

                // kadıköy başlığı
                entryRepository.save(Entry.builder()
                                .content(
                                                "kadıköy'de maç izlemek başka bir duygu. tribünlerde o müthiş atmosferi yaşamak için kilometrelerce yol gidilir.")
                                .topic(topic5)
                                .author(user1)
                                .likeCount(234)
                                .build());

                entryRepository.save(Entry.builder()
                                .content("istanbul'un en güzel semtlerinden biri. fenerbahçe ruhu her köşesinde hissediliyor.")
                                .topic(topic5)
                                .author(user2)
                                .likeCount(178)
                                .build());

                log.info("10 entry oluşturuldu");
                log.info("✅ Test verileri başarıyla yüklendi!");
        }
}
