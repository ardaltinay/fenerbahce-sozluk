import { ref, watch, onMounted } from 'vue'

/**
 * SEO meta tag yönetimi için composable
 * Sayfa bazlı dinamik meta tag güncellemesi sağlar
 */
export function useSeoMeta() {
  const defaultMeta = {
    title: 'Fenerbahçe Sözlük | Fenerbahçeliler için Özgür Platform',
    description: 'Fenerbahçe Sözlük - Fenerbahçeliler için özgür sözlük platformu. Fenerbahçe haberleri, maç yorumları, transfer söylentileri. Tartış, paylaş, Fenerbahçeli ol!',
    image: 'https://fenerbahcesozluk.net/og-image.png',
    url: 'https://fenerbahcesozluk.net'
  }

  /**
   * Meta tag'leri güncelle
   * @param {Object} meta - { title, description, image, url, type }
   */
  function setMeta(meta = {}) {
    const finalMeta = { ...defaultMeta, ...meta }

    // Title
    document.title = finalMeta.title
    updateMetaTag('name', 'title', finalMeta.title)

    // Description
    updateMetaTag('name', 'description', finalMeta.description)

    // Open Graph
    updateMetaTag('property', 'og:title', finalMeta.title)
    updateMetaTag('property', 'og:description', finalMeta.description)
    updateMetaTag('property', 'og:image', finalMeta.image)
    updateMetaTag('property', 'og:url', finalMeta.url)
    updateMetaTag('property', 'og:type', finalMeta.type || 'website')

    // Twitter
    updateMetaTag('name', 'twitter:title', finalMeta.title)
    updateMetaTag('name', 'twitter:description', finalMeta.description)
    updateMetaTag('name', 'twitter:image', finalMeta.image)
    updateMetaTag('name', 'twitter:url', finalMeta.url)

    // Canonical
    updateCanonical(finalMeta.url)
  }

  /**
   * Meta tag güncelleme helper
   */
  function updateMetaTag(attribute, key, value) {
    let element = document.querySelector(`meta[${attribute}="${key}"]`)
    if (element) {
      element.setAttribute('content', value)
    } else {
      element = document.createElement('meta')
      element.setAttribute(attribute, key)
      element.setAttribute('content', value)
      document.head.appendChild(element)
    }
  }

  /**
   * Canonical URL güncelleme
   */
  function updateCanonical(url) {
    let link = document.querySelector('link[rel="canonical"]')
    if (link) {
      link.setAttribute('href', url)
    } else {
      link = document.createElement('link')
      link.setAttribute('rel', 'canonical')
      link.setAttribute('href', url)
      document.head.appendChild(link)
    }
  }

  /**
   * Başlık sayfası için meta tag'ler
   */
  function setTopicMeta(topic) {
    setMeta({
      title: `${topic.title} - Fenerbahçe Sözlük`,
      description: `${topic.title} hakkında ${topic.entryCount || 0} entry. Fenerbahçe Sözlük'te tartışmaya katıl!`,
      url: `https://fenerbahcesozluk.net/baslik/${topic.id}/${encodeURIComponent(topic.title)}`,
      type: 'article'
    })
  }

  /**
   * Yazar profili için meta tag'ler
   */
  function setAuthorMeta(author) {
    setMeta({
      title: `${author.username} - Fenerbahçe Sözlük`,
      description: `${author.username} kullanıcısının Fenerbahçe Sözlük profili. ${author.entryCount || 0} entry yazdı.`,
      url: `https://fenerbahcesozluk.net/yazar/${author.username}`,
      type: 'profile'
    })
  }

  /**
   * Entry detay sayfası için meta tag'ler
   */
  function setEntryMeta(entry, topicTitle) {
    const shortContent = entry.content?.substring(0, 150) + (entry.content?.length > 150 ? '...' : '')
    setMeta({
      title: `${topicTitle} - Entry #${entry.id} - Fenerbahçe Sözlük`,
      description: shortContent,
      url: `https://fenerbahcesozluk.net/entry/${entry.id}`,
      type: 'article'
    })
  }

  /**
   * Arama sayfası için meta tag'ler
   */
  function setSearchMeta(query) {
    setMeta({
      title: `"${query}" araması - Fenerbahçe Sözlük`,
      description: `"${query}" için arama sonuçları. Fenerbahçe Sözlük'te ara, bul, tartış!`,
      url: `https://fenerbahcesozluk.net/ara?q=${encodeURIComponent(query)}`
    })
  }

  /**
   * Varsayılan meta'ya dön
   */
  function resetMeta() {
    setMeta(defaultMeta)
  }

  return {
    setMeta,
    setTopicMeta,
    setAuthorMeta,
    setEntryMeta,
    setSearchMeta,
    resetMeta
  }
}
